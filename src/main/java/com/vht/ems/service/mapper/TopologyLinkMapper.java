package com.vht.ems.service.mapper;

import com.vht.ems.domain.Device;
import com.vht.ems.domain.TopologyLink;
import com.vht.ems.service.dto.DeviceDTO;
import com.vht.ems.service.dto.TopologyLinkDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TopologyLink} and its DTO {@link TopologyLinkDTO}.
 */
@Mapper(componentModel = "spring")
public interface TopologyLinkMapper extends EntityMapper<TopologyLinkDTO, TopologyLink> {
    @Mapping(target = "sourceDevice", source = "sourceDevice", qualifiedByName = "deviceName")
    @Mapping(target = "targetDevice", source = "targetDevice", qualifiedByName = "deviceName")
    TopologyLinkDTO toDto(TopologyLink s);

    @Named("deviceName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    DeviceDTO toDtoDeviceName(Device device);
}
