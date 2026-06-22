package com.vht.ems.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vht.ems.domain.enumeration.AlertStatus;
import com.vht.ems.domain.enumeration.MetricType;
import com.vht.ems.domain.enumeration.Severity;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A AlertEvent.
 */
@Document(collection = "alert_event")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlertEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("metric_type")
    private MetricType metricType;

    @NotNull
    @Field("value")
    private Double value;

    @NotNull
    @Field("severity")
    private Severity severity;

    @NotNull
    @Size(max = 1000)
    @Field("message")
    private String message;

    @NotNull
    @Field("triggered_at")
    private Instant triggeredAt;

    @Field("resolved_at")
    private Instant resolvedAt;

    @NotNull
    @Field("status")
    private AlertStatus status;

    @DBRef
    @Field("device")
    @JsonIgnoreProperties(value = { "credential" }, allowSetters = true)
    private Device device;

    @DBRef
    @Field("rule")
    private AlertRule rule;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public AlertEvent id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MetricType getMetricType() {
        return this.metricType;
    }

    public AlertEvent metricType(MetricType metricType) {
        this.setMetricType(metricType);
        return this;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public Double getValue() {
        return this.value;
    }

    public AlertEvent value(Double value) {
        this.setValue(value);
        return this;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Severity getSeverity() {
        return this.severity;
    }

    public AlertEvent severity(Severity severity) {
        this.setSeverity(severity);
        return this;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return this.message;
    }

    public AlertEvent message(String message) {
        this.setMessage(message);
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTriggeredAt() {
        return this.triggeredAt;
    }

    public AlertEvent triggeredAt(Instant triggeredAt) {
        this.setTriggeredAt(triggeredAt);
        return this;
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public Instant getResolvedAt() {
        return this.resolvedAt;
    }

    public AlertEvent resolvedAt(Instant resolvedAt) {
        this.setResolvedAt(resolvedAt);
        return this;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public AlertStatus getStatus() {
        return this.status;
    }

    public AlertEvent status(AlertStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public Device getDevice() {
        return this.device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public AlertEvent device(Device device) {
        this.setDevice(device);
        return this;
    }

    public AlertRule getRule() {
        return this.rule;
    }

    public void setRule(AlertRule alertRule) {
        this.rule = alertRule;
    }

    public AlertEvent rule(AlertRule alertRule) {
        this.setRule(alertRule);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlertEvent)) {
            return false;
        }
        return getId() != null && getId().equals(((AlertEvent) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlertEvent{" +
            "id=" + getId() +
            ", metricType='" + getMetricType() + "'" +
            ", value=" + getValue() +
            ", severity='" + getSeverity() + "'" +
            ", message='" + getMessage() + "'" +
            ", triggeredAt='" + getTriggeredAt() + "'" +
            ", resolvedAt='" + getResolvedAt() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
