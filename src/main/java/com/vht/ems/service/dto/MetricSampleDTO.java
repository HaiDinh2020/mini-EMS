package com.vht.ems.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.vht.ems.domain.MetricSample} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MetricSampleDTO implements Serializable {

    private String id;

    private Double cpuUsage;

    private Double ramUsage;

    private Double diskUsage;

    private Double pingLatencyMs;

    @NotNull
    private Instant collectedAt;

    private DeviceDTO device;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getRamUsage() {
        return ramUsage;
    }

    public void setRamUsage(Double ramUsage) {
        this.ramUsage = ramUsage;
    }

    public Double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Double getPingLatencyMs() {
        return pingLatencyMs;
    }

    public void setPingLatencyMs(Double pingLatencyMs) {
        this.pingLatencyMs = pingLatencyMs;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(Instant collectedAt) {
        this.collectedAt = collectedAt;
    }

    public DeviceDTO getDevice() {
        return device;
    }

    public void setDevice(DeviceDTO device) {
        this.device = device;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetricSampleDTO)) {
            return false;
        }

        MetricSampleDTO metricSampleDTO = (MetricSampleDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, metricSampleDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MetricSampleDTO{" +
            "id='" + getId() + "'" +
            ", cpuUsage=" + getCpuUsage() +
            ", ramUsage=" + getRamUsage() +
            ", diskUsage=" + getDiskUsage() +
            ", pingLatencyMs=" + getPingLatencyMs() +
            ", collectedAt='" + getCollectedAt() + "'" +
            ", device=" + getDevice() +
            "}";
    }
}
