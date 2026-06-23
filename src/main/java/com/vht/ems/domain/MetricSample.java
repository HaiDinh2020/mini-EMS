package com.vht.ems.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A MetricSample.
 */
@Document(collection = "metric_sample")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MetricSample implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed
    @Field("device_id")
    private String deviceId;

    @Field("cpu_usage")
    private Double cpuUsage;

    @Field("ram_usage")
    private Double ramUsage;

    @Field("disk_usage")
    private Double diskUsage;

    @Field("ping_latency_ms")
    private Double pingLatencyMs;

    @NotNull
    @Field("collected_at")
    private Instant collectedAt;

    @DBRef
    @Field("device")
    @JsonIgnoreProperties(value = { "credential" }, allowSetters = true)
    private Device device;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public MetricSample id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public MetricSample deviceId(String deviceId) {
        this.setDeviceId(deviceId);
        return this;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getCpuUsage() {
        return this.cpuUsage;
    }

    public MetricSample cpuUsage(Double cpuUsage) {
        this.setCpuUsage(cpuUsage);
        return this;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getRamUsage() {
        return this.ramUsage;
    }

    public MetricSample ramUsage(Double ramUsage) {
        this.setRamUsage(ramUsage);
        return this;
    }

    public void setRamUsage(Double ramUsage) {
        this.ramUsage = ramUsage;
    }

    public Double getDiskUsage() {
        return this.diskUsage;
    }

    public MetricSample diskUsage(Double diskUsage) {
        this.setDiskUsage(diskUsage);
        return this;
    }

    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Double getPingLatencyMs() {
        return this.pingLatencyMs;
    }

    public MetricSample pingLatencyMs(Double pingLatencyMs) {
        this.setPingLatencyMs(pingLatencyMs);
        return this;
    }

    public void setPingLatencyMs(Double pingLatencyMs) {
        this.pingLatencyMs = pingLatencyMs;
    }

    public Instant getCollectedAt() {
        return this.collectedAt;
    }

    public MetricSample collectedAt(Instant collectedAt) {
        this.setCollectedAt(collectedAt);
        return this;
    }

    public void setCollectedAt(Instant collectedAt) {
        this.collectedAt = collectedAt;
    }

    public Device getDevice() {
        return this.device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public MetricSample device(Device device) {
        this.setDevice(device);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetricSample)) {
            return false;
        }
        return getId() != null && getId().equals(((MetricSample) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MetricSample{" +
            "id=" + getId() +
            ", deviceId='" + getDeviceId() + "'" +
            ", cpuUsage=" + getCpuUsage() +
            ", ramUsage=" + getRamUsage() +
            ", diskUsage=" + getDiskUsage() +
            ", pingLatencyMs=" + getPingLatencyMs() +
            ", collectedAt='" + getCollectedAt() + "'" +
            "}";
    }
}
