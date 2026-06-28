package com.vht.ems.config.dbmigrations;

import com.vht.ems.domain.Device;
import com.vht.ems.domain.enumeration.DeviceStatus;
import com.vht.ems.domain.enumeration.DeviceType;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * Seeds 5 simulated 5G Core network devices (gNodeB, AMF, SMF, UPF, UDM).
 * Devices use fixed IPs in subnet 172.28.0.0/24 and listen on TCP port 2022.
 * No SSH credential is set; the collector uses TCP-only mode for these nodes.
 */
@ChangeUnit(id = "5g-devices-initialization", order = "003")
public class DeviceSeederMigration {

    private final MongoTemplate template;

    public DeviceSeederMigration(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        List<Device> devices = buildDevices();
        for (Device device : devices) {
            boolean exists = template.exists(Query.query(Criteria.where("ip_address").is(device.getIpAddress())), Device.class);
            if (!exists) {
                template.save(device);
            }
        }
    }

    @RollbackExecution
    public void rollback() {
        List<String> ips = List.of("172.28.0.11", "172.28.0.12", "172.28.0.13", "172.28.0.14", "172.28.0.15");
        for (String ip : ips) {
            template.remove(Query.query(Criteria.where("ip_address").is(ip)), Device.class);
        }
    }

    private List<Device> buildDevices() {
        return List.of(
            build("device-5g-gnodeb", "gNodeB", "172.28.0.11", DeviceType.GNODEB, "5G Radio Access Node", "Ericsson", "RAN-6631"),
            build("device-5g-amf", "AMF", "172.28.0.12", DeviceType.AMF, "Access & Mobility Management Function", "Nokia", "AirFrame-AMF"),
            build("device-5g-smf", "SMF", "172.28.0.13", DeviceType.SMF, "Session Management Function", "Huawei", "CloudCore-SMF"),
            build("device-5g-upf", "UPF", "172.28.0.14", DeviceType.UPF, "User Plane Function", "Cisco", "ASR-5K-UPF"),
            build("device-5g-udm", "UDM", "172.28.0.15", DeviceType.UDM, "Unified Data Management", "Ericsson", "UDM-6.0")
        );
    }

    private Device build(String id, String name, String ip, DeviceType type, String desc, String vendor, String model) {
        Device d = new Device();
        d.setId(id);
        d.setName(name);
        d.setIpAddress(ip);
        d.setDeviceType(type);
        d.setDescription(desc);
        d.setVendor(vendor);
        d.setModel(model);
        d.setSshPort(2022);
        d.setStatus(DeviceStatus.UNKNOWN);
        d.setMonitoringEnabled(true);
        d.setLastCheckedAt(Instant.now());
        return d;
    }
}
