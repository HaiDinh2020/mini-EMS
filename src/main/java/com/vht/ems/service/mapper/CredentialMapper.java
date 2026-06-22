package com.vht.ems.service.mapper;

import com.vht.ems.domain.Credential;
import com.vht.ems.service.dto.CredentialDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Credential} and its DTO {@link CredentialDTO}.
 */
@Mapper(componentModel = "spring")
public interface CredentialMapper extends EntityMapper<CredentialDTO, Credential> {}
