package com.vht.ems.service.mapper;

import com.vht.ems.domain.Credential;
import com.vht.ems.domain.Device;
import com.vht.ems.service.dto.CredentialDTO;
import com.vht.ems.service.dto.DeviceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Device} and its DTO {@link DeviceDTO}.
 */
@Mapper(componentModel = "spring")
public interface DeviceMapper extends EntityMapper<DeviceDTO, Device> {
    @Mapping(target = "credential", source = "credential", qualifiedByName = "credentialName")
    DeviceDTO toDto(Device s);

    @Named("credentialName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CredentialDTO toDtoCredentialName(Credential credential);
}
