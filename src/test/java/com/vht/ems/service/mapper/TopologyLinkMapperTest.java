package com.vht.ems.service.mapper;

import static com.vht.ems.domain.TopologyLinkAsserts.*;
import static com.vht.ems.domain.TopologyLinkTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TopologyLinkMapperTest {

    private TopologyLinkMapper topologyLinkMapper;

    @BeforeEach
    void setUp() {
        topologyLinkMapper = new TopologyLinkMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTopologyLinkSample1();
        var actual = topologyLinkMapper.toEntity(topologyLinkMapper.toDto(expected));
        assertTopologyLinkAllPropertiesEquals(expected, actual);
    }
}
