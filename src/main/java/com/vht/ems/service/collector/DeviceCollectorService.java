package com.vht.ems.service.collector;

import com.vht.ems.domain.Credential;
import com.vht.ems.domain.Device;
import com.vht.ems.domain.MetricSample;
import com.vht.ems.domain.enumeration.DeviceStatus;
import com.vht.ems.repository.DeviceRepository;
import com.vht.ems.repository.MetricSampleRepository;
import com.vht.ems.service.AlertEvaluatorService;
import com.vht.ems.service.DeviceStatusPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DeviceCollectorService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceCollectorService.class);

    private final DeviceRepository deviceRepository;
    private final MetricSampleRepository metricSampleRepository;
    private final AlertEvaluatorService alertEvaluatorService;
    private final DeviceStatusPublisher deviceStatusPublisher;
    private final StringEncryptor stringEncryptor;
    private final Executor collectorExecutor;

    private final AtomicLong onlineCount = new AtomicLong(0);
    private final AtomicLong offlineCount = new AtomicLong(0);

    @Value("${ems.collector.tcp-timeout-ms:2000}")
    private int tcpTimeoutMs;

    @Value("${ems.collector.ssh-timeout-ms:10000}")
    private int sshTimeoutMs;

    public DeviceCollectorService(
        DeviceRepository deviceRepository,
        MetricSampleRepository metricSampleRepository,
        AlertEvaluatorService alertEvaluatorService,
        DeviceStatusPublisher deviceStatusPublisher,
        StringEncryptor stringEncryptor,
        MeterRegistry meterRegistry,
        @Qualifier("collectorExecutor") Executor collectorExecutor
    ) {
        this.deviceRepository = deviceRepository;
        this.metricSampleRepository = metricSampleRepository;
        this.alertEvaluatorService = alertEvaluatorService;
        this.deviceStatusPublisher = deviceStatusPublisher;
        this.stringEncryptor = stringEncryptor;
        this.collectorExecutor = collectorExecutor;
        meterRegistry.gauge("ems.device.online.count", onlineCount, AtomicLong::doubleValue);
        meterRegistry.gauge("ems.device.offline.count", offlineCount, AtomicLong::doubleValue);
    }

    @Scheduled(fixedDelayString = "${ems.collector.interval-ms:60000}")
    public void collectAll() {
        List<Device> devices = deviceRepository.findByMonitoringEnabledTrue();
        if (devices.isEmpty()) {
            LOG.debug("Collector: no devices with monitoring enabled");
            return;
        }
        LOG.info("Collector: starting scan for {} device(s)", devices.size());

        List<CompletableFuture<Void>> futures = devices
            .stream()
            .map(device -> CompletableFuture.runAsync(() -> collectDevice(device), collectorExecutor))
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        LOG.info("Collector: scan complete");
        onlineCount.set(deviceRepository.countByStatus(DeviceStatus.ONLINE));
        offlineCount.set(deviceRepository.countByStatus(DeviceStatus.OFFLINE));
    }

    // --- per-device ---

    private void collectDevice(Device device) {
        try {
            int port = device.getSshPort() != null ? device.getSshPort() : 22;
            long tcpStart = System.currentTimeMillis();
            boolean reachable = tcpReachable(device.getIpAddress(), port, tcpTimeoutMs);
            double latencyMs = System.currentTimeMillis() - tcpStart;

            if (!reachable) {
                LOG.warn(
                    "Collector: device {} ({}) is unreachable on TCP {}:{}",
                    device.getName(),
                    device.getId(),
                    device.getIpAddress(),
                    port
                );
                updateDeviceStatus(device, DeviceStatus.OFFLINE);
                saveOfflineMetric(device, latencyMs);
                return;
            }

            Credential credential = device.getCredential();
            if (credential == null) {
                LOG.debug("Collector: device {} has no credential, TCP-reachable → ONLINE", device.getName());
                updateDeviceStatus(device, DeviceStatus.ONLINE);
                return;
            }

            collectViaSsh(device, credential, latencyMs);
        } catch (Exception e) {
            LOG.warn("Collector: unexpected error for device {}: {}", device.getName(), e.getMessage());
            updateDeviceStatus(device, DeviceStatus.UNKNOWN);
        }
    }

    private void collectViaSsh(Device device, Credential credential, double tcpLatencyMs) {
        String plainPassword = decryptSecret(credential.getEncryptedSecret());
        int port = device.getSshPort() != null ? device.getSshPort() : 22;

        try (SSHClient ssh = new SSHClient()) {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.setConnectTimeout(sshTimeoutMs);
            ssh.setTimeout(sshTimeoutMs);
            ssh.connect(device.getIpAddress(), port);
            ssh.authPassword(credential.getUsername(), plainPassword);

            String stat1 = runCommand(ssh, "cat /proc/stat");
            Thread.sleep(1000);
            String stat2 = runCommand(ssh, "cat /proc/stat");
            String freeOut = runCommand(ssh, "free -m");
            String dfOut = runCommand(ssh, "df -h /");

            double cpu = MetricParser.parseCpuUsagePercent(stat1, stat2);
            double ram = MetricParser.parseRamUsagePercent(freeOut);
            double disk = MetricParser.parseDiskUsagePercent(dfOut);

            MetricSample sample = new MetricSample()
                .deviceId(device.getId())
                .device(device)
                .cpuUsage(cpu)
                .ramUsage(ram)
                .diskUsage(disk)
                .pingLatencyMs(tcpLatencyMs)
                .collectedAt(Instant.now());

            metricSampleRepository.save(sample);
            alertEvaluatorService.evaluate(device, sample);

            device.setStatus(DeviceStatus.ONLINE);
            device.setLastCheckedAt(Instant.now());
            saveDevice(device);

            LOG.debug("Collector: device {} — cpu={:.1f}% ram={:.1f}% disk={:.1f}%", device.getName(), cpu, ram, disk);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Collector: SSH interrupted for device {}", device.getName());
            updateDeviceStatus(device, DeviceStatus.UNKNOWN);
        } catch (IOException e) {
            LOG.warn("Collector: SSH failed for device {}: {}", device.getName(), e.getMessage());
            updateDeviceStatus(device, DeviceStatus.UNKNOWN);
        } finally {
            // Wipe the decrypted password from the local variable scope (GC will clear)
            plainPassword = null;
        }
    }

    // --- helpers ---

    private boolean tcpReachable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String runCommand(SSHClient ssh, String command) throws IOException {
        try (Session session = ssh.startSession(); Session.Command cmd = session.exec(command)) {
            String output = IOUtils.readFully(cmd.getInputStream()).toString();
            cmd.join(sshTimeoutMs, TimeUnit.MILLISECONDS);
            return output;
        }
    }

    private void updateDeviceStatus(Device device, DeviceStatus status) {
        device.setStatus(status);
        device.setLastCheckedAt(Instant.now());
        saveDevice(device);
    }

    private void saveDevice(Device device) {
        deviceRepository.save(device);
        deviceStatusPublisher.publishDeviceStatus(device);
    }

    private void saveOfflineMetric(Device device, double latencyMs) {
        MetricSample sample = new MetricSample()
            .deviceId(device.getId())
            .device(device)
            .pingLatencyMs(latencyMs)
            .collectedAt(Instant.now());
        metricSampleRepository.save(sample);
        alertEvaluatorService.evaluate(device, sample);
    }

    /**
     * Decrypt a Jasypt-encrypted secret.
     * If the value is not wrapped in ENC(...), it is returned as-is
     * (supports seed data stored without encryption during development).
     */
    private String decryptSecret(String encryptedSecret) {
        if (encryptedSecret == null) return "";
        if (encryptedSecret.startsWith("ENC(") && encryptedSecret.endsWith(")")) {
            String inner = encryptedSecret.substring(4, encryptedSecret.length() - 1);
            return stringEncryptor.decrypt(inner);
        }
        return encryptedSecret;
    }
}
