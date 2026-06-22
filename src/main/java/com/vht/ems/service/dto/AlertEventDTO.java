package com.vht.ems.service.dto;

import com.vht.ems.domain.enumeration.AlertStatus;
import com.vht.ems.domain.enumeration.MetricType;
import com.vht.ems.domain.enumeration.Severity;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.vht.ems.domain.AlertEvent} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlertEventDTO implements Serializable {

    private String id;

    @NotNull
    private MetricType metricType;

    @NotNull
    private Double value;

    @NotNull
    private Severity severity;

    @NotNull
    @Size(max = 1000)
    private String message;

    @NotNull
    private Instant triggeredAt;

    private Instant resolvedAt;

    @NotNull
    private AlertStatus status;

    private DeviceDTO device;

    private AlertRuleDTO rule;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public DeviceDTO getDevice() {
        return device;
    }

    public void setDevice(DeviceDTO device) {
        this.device = device;
    }

    public AlertRuleDTO getRule() {
        return rule;
    }

    public void setRule(AlertRuleDTO rule) {
        this.rule = rule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlertEventDTO)) {
            return false;
        }

        AlertEventDTO alertEventDTO = (AlertEventDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, alertEventDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlertEventDTO{" +
            "id='" + getId() + "'" +
            ", metricType='" + getMetricType() + "'" +
            ", value=" + getValue() +
            ", severity='" + getSeverity() + "'" +
            ", message='" + getMessage() + "'" +
            ", triggeredAt='" + getTriggeredAt() + "'" +
            ", resolvedAt='" + getResolvedAt() + "'" +
            ", status='" + getStatus() + "'" +
            ", device=" + getDevice() +
            ", rule=" + getRule() +
            "}";
    }
}
