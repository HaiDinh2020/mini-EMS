package com.vht.ems.service.mapper;

import static com.vht.ems.domain.AlertRuleAsserts.*;
import static com.vht.ems.domain.AlertRuleTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlertRuleMapperTest {

    private AlertRuleMapper alertRuleMapper;

    @BeforeEach
    void setUp() {
        alertRuleMapper = new AlertRuleMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAlertRuleSample1();
        var actual = alertRuleMapper.toEntity(alertRuleMapper.toDto(expected));
        assertAlertRuleAllPropertiesEquals(expected, actual);
    }
}
