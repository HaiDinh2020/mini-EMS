package com.vht.ems.domain;

import static com.vht.ems.domain.AlertEventTestSamples.*;
import static com.vht.ems.domain.AlertRuleTestSamples.*;
import static com.vht.ems.domain.DeviceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.vht.ems.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlertEventTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlertEvent.class);
        AlertEvent alertEvent1 = getAlertEventSample1();
        AlertEvent alertEvent2 = new AlertEvent();
        assertThat(alertEvent1).isNotEqualTo(alertEvent2);

        alertEvent2.setId(alertEvent1.getId());
        assertThat(alertEvent1).isEqualTo(alertEvent2);

        alertEvent2 = getAlertEventSample2();
        assertThat(alertEvent1).isNotEqualTo(alertEvent2);
    }

    @Test
    void deviceTest() {
        AlertEvent alertEvent = getAlertEventRandomSampleGenerator();
        Device deviceBack = getDeviceRandomSampleGenerator();

        alertEvent.setDevice(deviceBack);
        assertThat(alertEvent.getDevice()).isEqualTo(deviceBack);

        alertEvent.device(null);
        assertThat(alertEvent.getDevice()).isNull();
    }

    @Test
    void ruleTest() {
        AlertEvent alertEvent = getAlertEventRandomSampleGenerator();
        AlertRule alertRuleBack = getAlertRuleRandomSampleGenerator();

        alertEvent.setRule(alertRuleBack);
        assertThat(alertEvent.getRule()).isEqualTo(alertRuleBack);

        alertEvent.rule(null);
        assertThat(alertEvent.getRule()).isNull();
    }
}
