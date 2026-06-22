package com.vht.ems.domain;

import static com.vht.ems.domain.DeviceTestSamples.*;
import static com.vht.ems.domain.MetricSampleTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.vht.ems.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MetricSampleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MetricSample.class);
        MetricSample metricSample1 = getMetricSampleSample1();
        MetricSample metricSample2 = new MetricSample();
        assertThat(metricSample1).isNotEqualTo(metricSample2);

        metricSample2.setId(metricSample1.getId());
        assertThat(metricSample1).isEqualTo(metricSample2);

        metricSample2 = getMetricSampleSample2();
        assertThat(metricSample1).isNotEqualTo(metricSample2);
    }

    @Test
    void deviceTest() {
        MetricSample metricSample = getMetricSampleRandomSampleGenerator();
        Device deviceBack = getDeviceRandomSampleGenerator();

        metricSample.setDevice(deviceBack);
        assertThat(metricSample.getDevice()).isEqualTo(deviceBack);

        metricSample.device(null);
        assertThat(metricSample.getDevice()).isNull();
    }
}
