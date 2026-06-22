package com.vht.ems.domain;

import java.util.UUID;

public class TopologyLinkTestSamples {

    public static TopologyLink getTopologyLinkSample1() {
        return new TopologyLink().id("id1").linkType("linkType1");
    }

    public static TopologyLink getTopologyLinkSample2() {
        return new TopologyLink().id("id2").linkType("linkType2");
    }

    public static TopologyLink getTopologyLinkRandomSampleGenerator() {
        return new TopologyLink().id(UUID.randomUUID().toString()).linkType(UUID.randomUUID().toString());
    }
}
