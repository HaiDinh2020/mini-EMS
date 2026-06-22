package com.vht.ems.service.mapper;

import static com.vht.ems.domain.AuditLogAsserts.*;
import static com.vht.ems.domain.AuditLogTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditLogMapperTest {

    private AuditLogMapper auditLogMapper;

    @BeforeEach
    void setUp() {
        auditLogMapper = new AuditLogMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAuditLogSample1();
        var actual = auditLogMapper.toEntity(auditLogMapper.toDto(expected));
        assertAuditLogAllPropertiesEquals(expected, actual);
    }
}
