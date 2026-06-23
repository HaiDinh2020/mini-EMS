package com.vht.ems.service.mapper;

import com.vht.ems.domain.Credential;
import com.vht.ems.domain.Device;
import com.vht.ems.service.dto.DeviceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Device} and its DTO {@link DeviceDTO}.
 */
@Mapper(componentModel = "spring")
public interface DeviceMapper extends EntityMapper<DeviceDTO, Device> {
    @Mapping(target = "credentialId", source = "credential.id")
    DeviceDTO toDto(Device s);

    @Mapping(target = "credential", source = "credentialId", qualifiedByName = "credentialFromId")
    Device toEntity(DeviceDTO deviceDTO);

    @Named("partialUpdate")
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "credential", source = "credentialId", qualifiedByName = "credentialFromId")
    void partialUpdate(@MappingTarget Device entity, DeviceDTO dto);

    @Named("credentialFromId")
    default Credential credentialFromId(String id) {
        if (id == null) return null;
        Credential c = new Credential();
        c.setId(id);
        return c;
    }
}
