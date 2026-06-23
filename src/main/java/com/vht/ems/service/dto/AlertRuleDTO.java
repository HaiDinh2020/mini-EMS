package com.vht.ems.service.dto;

import com.vht.ems.domain.enumeration.MetricType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.vht.ems.domain.AlertRule} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlertRuleDTO implements Serializable {

    private String id;

    @NotNull
    private MetricType metricType;

    @NotNull
    private Double thresholdWarning;

    @NotNull
    private Double thresholdCritical;

    @NotNull
    private Boolean enabled;

    private String deviceId;

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

    public Double getThresholdWarning() {
        return thresholdWarning;
    }

    public void setThresholdWarning(Double thresholdWarning) {
        this.thresholdWarning = thresholdWarning;
    }

    public Double getThresholdCritical() {
        return thresholdCritical;
    }

    public void setThresholdCritical(Double thresholdCritical) {
        this.thresholdCritical = thresholdCritical;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlertRuleDTO)) {
            return false;
        }

        AlertRuleDTO alertRuleDTO = (AlertRuleDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, alertRuleDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlertRuleDTO{" +
            "id='" + getId() + "'" +
            ", metricType='" + getMetricType() + "'" +
            ", thresholdWarning=" + getThresholdWarning() +
            ", thresholdCritical=" + getThresholdCritical() +
            ", enabled='" + getEnabled() + "'" +
            "}";
    }
}
