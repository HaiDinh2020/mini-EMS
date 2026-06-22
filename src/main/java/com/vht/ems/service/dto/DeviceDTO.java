package com.vht.ems.service.dto;

import com.vht.ems.domain.enumeration.DeviceStatus;
import com.vht.ems.domain.enumeration.DeviceType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.vht.ems.domain.Device} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DeviceDTO implements Serializable {

    private String id;

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 255)
    @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")
    private String ipAddress;

    @Size(max = 255)
    private String hostname;

    @NotNull
    private DeviceType deviceType;

    @Size(max = 255)
    private String vendor;

    @Size(max = 255)
    private String model;

    private Integer sshPort;

    @Size(max = 255)
    private String sshUsername;

    @Size(max = 500)
    private String location;

    @NotNull
    private DeviceStatus status;

    private Instant lastCheckedAt;

    @NotNull
    private Boolean monitoringEnabled;

    private String description;

    private CredentialDTO credential;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
    }

    public String getSshUsername() {
        return sshUsername;
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername = sshUsername;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public Boolean getMonitoringEnabled() {
        return monitoringEnabled;
    }

    public void setMonitoringEnabled(Boolean monitoringEnabled) {
        this.monitoringEnabled = monitoringEnabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CredentialDTO getCredential() {
        return credential;
    }

    public void setCredential(CredentialDTO credential) {
        this.credential = credential;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceDTO)) {
            return false;
        }

        DeviceDTO deviceDTO = (DeviceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, deviceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DeviceDTO{" +
            "id='" + getId() + "'" +
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
            ", credential=" + getCredential() +
            "}";
    }
}
