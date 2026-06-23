package com.vht.ems.config.dbmigrations;

import com.vht.ems.domain.AlertRule;
import com.vht.ems.domain.enumeration.MetricType;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Seeds default alert rules for CPU, RAM, and Disk metrics.
 * Only runs once (Mongock ensures idempotency).
 */
@ChangeUnit(id = "alert-rules-initialization", order = "002")
public class AlertRuleSeederMigration {

    private final MongoTemplate template;

    public AlertRuleSeederMigration(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        template.save(buildRule(MetricType.CPU, 85.0, 95.0));
        template.save(buildRule(MetricType.RAM, 80.0, 90.0));
        template.save(buildRule(MetricType.DISK, 75.0, 90.0));
    }

    @RollbackExecution
    public void rollback() {
        template.getCollection("alert_rule").deleteMany(new org.bson.Document("device_id", null));
    }

    private AlertRule buildRule(MetricType metricType, double warning, double critical) {
        AlertRule rule = new AlertRule();
        rule.setMetricType(metricType);
        rule.setThresholdWarning(warning);
        rule.setThresholdCritical(critical);
        rule.setEnabled(true);
        return rule;
    }
}
