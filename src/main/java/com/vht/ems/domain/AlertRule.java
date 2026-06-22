package com.vht.ems.domain;

import com.vht.ems.domain.enumeration.MetricType;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A AlertRule.
 */
@Document(collection = "alert_rule")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlertRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("metric_type")
    private MetricType metricType;

    @NotNull
    @Field("threshold_warning")
    private Double thresholdWarning;

    @NotNull
    @Field("threshold_critical")
    private Double thresholdCritical;

    @NotNull
    @Field("enabled")
    private Boolean enabled;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public AlertRule id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MetricType getMetricType() {
        return this.metricType;
    }

    public AlertRule metricType(MetricType metricType) {
        this.setMetricType(metricType);
        return this;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public Double getThresholdWarning() {
        return this.thresholdWarning;
    }

    public AlertRule thresholdWarning(Double thresholdWarning) {
        this.setThresholdWarning(thresholdWarning);
        return this;
    }

    public void setThresholdWarning(Double thresholdWarning) {
        this.thresholdWarning = thresholdWarning;
    }

    public Double getThresholdCritical() {
        return this.thresholdCritical;
    }

    public AlertRule thresholdCritical(Double thresholdCritical) {
        this.setThresholdCritical(thresholdCritical);
        return this;
    }

    public void setThresholdCritical(Double thresholdCritical) {
        this.thresholdCritical = thresholdCritical;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public AlertRule enabled(Boolean enabled) {
        this.setEnabled(enabled);
        return this;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlertRule)) {
            return false;
        }
        return getId() != null && getId().equals(((AlertRule) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlertRule{" +
            "id=" + getId() +
            ", metricType='" + getMetricType() + "'" +
            ", thresholdWarning=" + getThresholdWarning() +
            ", thresholdCritical=" + getThresholdCritical() +
            ", enabled='" + getEnabled() + "'" +
            "}";
    }
}
