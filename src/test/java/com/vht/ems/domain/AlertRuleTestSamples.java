package com.vht.ems.domain;

import java.util.UUID;

public class AlertRuleTestSamples {

    public static AlertRule getAlertRuleSample1() {
        return new AlertRule().id("id1");
    }

    public static AlertRule getAlertRuleSample2() {
        return new AlertRule().id("id2");
    }

    public static AlertRule getAlertRuleRandomSampleGenerator() {
        return new AlertRule().id(UUID.randomUUID().toString());
    }
}
