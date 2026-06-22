package com.vht.ems.domain;

import java.util.UUID;

public class AlertEventTestSamples {

    public static AlertEvent getAlertEventSample1() {
        return new AlertEvent().id("id1").message("message1");
    }

    public static AlertEvent getAlertEventSample2() {
        return new AlertEvent().id("id2").message("message2");
    }

    public static AlertEvent getAlertEventRandomSampleGenerator() {
        return new AlertEvent().id(UUID.randomUUID().toString()).message(UUID.randomUUID().toString());
    }
}
