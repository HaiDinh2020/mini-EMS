package com.vht.ems.service.mapper;

import com.vht.ems.domain.Device;
import com.vht.ems.domain.MetricSample;
import com.vht.ems.service.dto.DeviceDTO;
import com.vht.ems.service.dto.MetricSampleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MetricSample} and its DTO {@link MetricSampleDTO}.
 */
@Mapper(componentModel = "spring")
public interface MetricSampleMapper extends EntityMapper<MetricSampleDTO, MetricSample> {
    @Mapping(target = "device", source = "device", qualifiedByName = "deviceName")
    @Mapping(target = "deviceId", source = "deviceId")
    MetricSampleDTO toDto(MetricSample s);

    @Named("deviceName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    DeviceDTO toDtoDeviceName(Device device);
}
