package com.vht.ems.domain;

import com.vht.ems.domain.enumeration.DeviceStatus;
import com.vht.ems.domain.enumeration.DeviceType;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Device.
 */
@Document(collection = "device")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Device implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(max = 255)
    @Field("name")
    private String name;

    @NotNull
    @Size(max = 255)
    @Field("ip_address")
    private String ipAddress;

    @Size(max = 255)
    @Field("hostname")
    private String hostname;

    @NotNull
    @Field("device_type")
    private DeviceType deviceType;

    @Size(max = 255)
    @Field("vendor")
    private String vendor;

    @Size(max = 255)
    @Field("model")
    private String model;

    @Field("ssh_port")
    private Integer sshPort;

    @Size(max = 255)
    @Field("ssh_username")
    private String sshUsername;

    @Size(max = 500)
    @Field("location")
    private String location;

    @NotNull
    @Field("status")
    private DeviceStatus status;

    @Field("last_checked_at")
    private Instant lastCheckedAt;

    @NotNull
    @Field("monitoring_enabled")
    private Boolean monitoringEnabled;

    @Field("description")
    private String description;

    @DBRef
    @Field("credential")
    private Credential credential;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public Device id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Device name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public Device ipAddress(String ipAddress) {
        this.setIpAddress(ipAddress);
        return this;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return this.hostname;
    }

    public Device hostname(String hostname) {
        this.setHostname(hostname);
        return this;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public Device deviceType(DeviceType deviceType) {
        this.setDeviceType(deviceType);
        return this;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getVendor() {
        return this.vendor;
    }

    public Device vendor(String vendor) {
        this.setVendor(vendor);
        return this;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return this.model;
    }

    public Device model(String model) {
        this.setModel(model);
        return this;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getSshPort() {
        return this.sshPort;
    }

    public Device sshPort(Integer sshPort) {
        this.setSshPort(sshPort);
        return this;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
    }

    public String getSshUsername() {
        return this.sshUsername;
    }

    public Device sshUsername(String sshUsername) {
        this.setSshUsername(sshUsername);
        return this;
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername = sshUsername;
    }

    public String getLocation() {
        return this.location;
    }

    public Device location(String location) {
        this.setLocation(location);
        return this;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DeviceStatus getStatus() {
        return this.status;
    }

    public Device status(DeviceStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public Instant getLastCheckedAt() {
        return this.lastCheckedAt;
    }

    public Device lastCheckedAt(Instant lastCheckedAt) {
        this.setLastCheckedAt(lastCheckedAt);
        return this;
    }

    public void setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public Boolean getMonitoringEnabled() {
        return this.monitoringEnabled;
    }

    public Device monitoringEnabled(Boolean monitoringEnabled) {
        this.setMonitoringEnabled(monitoringEnabled);
        return this;
    }

    public void setMonitoringEnabled(Boolean monitoringEnabled) {
        this.monitoringEnabled = monitoringEnabled;
    }

    public String getDescription() {
        return this.description;
    }

    public Device description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Credential getCredential() {
        return this.credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public Device credential(Credential credential) {
        this.setCredential(credential);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Device)) {
            return false;
        }
        return getId() != null && getId().equals(((Device) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Device{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", ipAddress='" + getIpAddress() + "'" +
            ", hostname='" + getHostname() + "'" +
            ", deviceType='" + getDeviceType() + "'" +
            ", vendor='" + getVendor() + "'" +
            ", model='" + getModel() + "'" +
            ", sshPort=" + getSshPort() +
            ", sshUsername='" + getSshUsername() + "'" +
            ", location='" + getLocation() + "'" +
            ", status='" + getStatus() + "'" +
            ", lastCheckedAt='" + getLastCheckedAt() + "'" +
            ", monitoringEnabled='" + getMonitoringEnabled() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
