package com.vht.ems.service.mapper;

import static com.vht.ems.domain.AlertEventAsserts.*;
import static com.vht.ems.domain.AlertEventTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlertEventMapperTest {

    private AlertEventMapper alertEventMapper;

    @BeforeEach
    void setUp() {
        alertEventMapper = new AlertEventMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAlertEventSample1();
        var actual = alertEventMapper.toEntity(alertEventMapper.toDto(expected));
        assertAlertEventAllPropertiesEquals(expected, actual);
    }
}
