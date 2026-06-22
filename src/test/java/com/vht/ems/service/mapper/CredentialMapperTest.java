package com.vht.ems.service.mapper;

import static com.vht.ems.domain.CredentialAsserts.*;
import static com.vht.ems.domain.CredentialTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CredentialMapperTest {

    private CredentialMapper credentialMapper;

    @BeforeEach
    void setUp() {
        credentialMapper = new CredentialMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCredentialSample1();
        var actual = credentialMapper.toEntity(credentialMapper.toDto(expected));
        assertCredentialAllPropertiesEquals(expected, actual);
    }
}
