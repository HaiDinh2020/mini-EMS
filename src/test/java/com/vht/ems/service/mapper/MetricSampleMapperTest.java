package com.vht.ems.service.mapper;

import static com.vht.ems.domain.MetricSampleAsserts.*;
import static com.vht.ems.domain.MetricSampleTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricSampleMapperTest {

    private MetricSampleMapper metricSampleMapper;

    @BeforeEach
    void setUp() {
        metricSampleMapper = new MetricSampleMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMetricSampleSample1();
        var actual = metricSampleMapper.toEntity(metricSampleMapper.toDto(expected));
        assertMetricSampleAllPropertiesEquals(expected, actual);
    }
}
