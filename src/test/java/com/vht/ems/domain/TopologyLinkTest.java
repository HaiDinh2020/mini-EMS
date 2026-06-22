package com.vht.ems.domain;

import static com.vht.ems.domain.DeviceTestSamples.*;
import static com.vht.ems.domain.TopologyLinkTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.vht.ems.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TopologyLinkTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TopologyLink.class);
        TopologyLink topologyLink1 = getTopologyLinkSample1();
        TopologyLink topologyLink2 = new TopologyLink();
        assertThat(topologyLink1).isNotEqualTo(topologyLink2);

        topologyLink2.setId(topologyLink1.getId());
        assertThat(topologyLink1).isEqualTo(topologyLink2);

        topologyLink2 = getTopologyLinkSample2();
        assertThat(topologyLink1).isNotEqualTo(topologyLink2);
    }

    @Test
    void sourceDeviceTest() {
        TopologyLink topologyLink = getTopologyLinkRandomSampleGenerator();
        Device deviceBack = getDeviceRandomSampleGenerator();

        topologyLink.setSourceDevice(deviceBack);
        assertThat(topologyLink.getSourceDevice()).isEqualTo(deviceBack);

        topologyLink.sourceDevice(null);
        assertThat(topologyLink.getSourceDevice()).isNull();
    }

    @Test
    void targetDeviceTest() {
        TopologyLink topologyLink = getTopologyLinkRandomSampleGenerator();
        Device deviceBack = getDeviceRandomSampleGenerator();

        topologyLink.setTargetDevice(deviceBack);
        assertThat(topologyLink.getTargetDevice()).isEqualTo(deviceBack);

        topologyLink.targetDevice(null);
        assertThat(topologyLink.getTargetDevice()).isNull();
    }
}
