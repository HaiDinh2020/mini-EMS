package com.vht.ems.domain;

import java.util.UUID;

public class MetricSampleTestSamples {

    public static MetricSample getMetricSampleSample1() {
        return new MetricSample().id("id1");
    }

    public static MetricSample getMetricSampleSample2() {
        return new MetricSample().id("id2");
    }

    public static MetricSample getMetricSampleRandomSampleGenerator() {
        return new MetricSample().id(UUID.randomUUID().toString());
    }
}
