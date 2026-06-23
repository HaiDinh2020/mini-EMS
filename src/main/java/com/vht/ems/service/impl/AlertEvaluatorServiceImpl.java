package com.vht.ems.service.impl;

import com.vht.ems.domain.AlertEvent;
import com.vht.ems.domain.AlertRule;
import com.vht.ems.domain.Device;
import com.vht.ems.domain.MetricSample;
import com.vht.ems.domain.enumeration.AlertStatus;
import com.vht.ems.domain.enumeration.MetricType;
import com.vht.ems.domain.enumeration.Severity;
import com.vht.ems.repository.AlertEventRepository;
import com.vht.ems.repository.AlertRuleRepository;
import com.vht.ems.service.AlertEvaluatorService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertEvaluatorServiceImpl implements AlertEvaluatorService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertEvaluatorServiceImpl.class);
    private static final String ALERTS_TOPIC = "/topic/alerts";

    private final AlertRuleRepository alertRuleRepository;
    private final AlertEventRepository alertEventRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AlertEvaluatorServiceImpl(
        AlertRuleRepository alertRuleRepository,
        AlertEventRepository alertEventRepository,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.alertRuleRepository = alertRuleRepository;
        this.alertEventRepository = alertEventRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void evaluate(Device device, MetricSample sample) {
        List<AlertRule> rules = alertRuleRepository.findEnabledRulesForDevices(List.of(device.getId()));
        for (AlertRule rule : rules) {
            Double value = extractValue(sample, rule.getMetricType());
            if (value == null) {
                continue;
            }
            if (value >= rule.getThresholdCritical()) {
                createOrUpdate(device, rule, Severity.CRITICAL, value);
            } else if (value >= rule.getThresholdWarning()) {
                createOrUpdate(device, rule, Severity.WARNING, value);
            } else {
                autoResolve(device, rule);
            }
        }
    }

    private void createOrUpdate(Device device, AlertRule rule, Severity severity, Double value) {
        Optional<AlertEvent> existing = alertEventRepository.findByDeviceAndRuleAndStatus(device, rule, AlertStatus.OPEN);
        AlertEvent event;
        if (existing.isPresent()) {
            event = existing.get();
            event.setValue(value);
            event.setTriggeredAt(Instant.now());
            event.setSeverity(severity);
        } else {
            event = new AlertEvent();
            event.setDevice(device);
            event.setRule(rule);
            event.setMetricType(rule.getMetricType());
            event.setValue(value);
            event.setSeverity(severity);
            event.setStatus(AlertStatus.OPEN);
            event.setTriggeredAt(Instant.now());
            event.setMessage(buildMessage(device.getName(), rule.getMetricType(), severity, value));
        }
        event = alertEventRepository.save(event);
        broadcastAlert(event);
    }

    private void autoResolve(Device device, AlertRule rule) {
        alertEventRepository.findByDeviceAndRuleAndStatus(device, rule, AlertStatus.OPEN).ifPresent(event -> {
            event.setStatus(AlertStatus.RESOLVED);
            event.setResolvedAt(Instant.now());
            event = alertEventRepository.save(event);
            broadcastAlert(event);
        });
    }

    private void broadcastAlert(AlertEvent event) {
        try {
            String deviceName = event.getDevice() != null ? event.getDevice().getName() : "";
            Map<String, Object> payload = Map.of(
                "type",
                "ALERT_EVENT",
                "alertEventId",
                event.getId(),
                "deviceId",
                event.getDevice() != null ? event.getDevice().getId() : "",
                "deviceName",
                deviceName,
                "metricType",
                event.getMetricType().name(),
                "severity",
                event.getSeverity().name(),
                "value",
                event.getValue(),
                "message",
                event.getMessage() != null ? event.getMessage() : "",
                "status",
                event.getStatus().name(),
                "timestamp",
                event.getTriggeredAt().toString()
            );
            messagingTemplate.convertAndSend(ALERTS_TOPIC, (Object) payload);
        } catch (Exception e) {
            LOG.warn("Failed to broadcast alert event {}: {}", event.getId(), e.getMessage());
        }
    }

    private Double extractValue(MetricSample sample, MetricType metricType) {
        return switch (metricType) {
            case CPU -> sample.getCpuUsage();
            case RAM -> sample.getRamUsage();
            case DISK -> sample.getDiskUsage();
            case PING_LATENCY -> sample.getPingLatencyMs();
            default -> null;
        };
    }

    private String buildMessage(String deviceName, MetricType metricType, Severity severity, Double value) {
        return String.format(
            "%s usage %.1f%% exceeds %s threshold on %s",
            metricType.name(),
            value,
            severity.name().toLowerCase(),
            deviceName != null ? deviceName : "unknown"
        );
    }
}
