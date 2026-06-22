package com.vht.ems.service.mapper;

import com.vht.ems.domain.AlertEvent;
import com.vht.ems.domain.AlertRule;
import com.vht.ems.domain.Device;
import com.vht.ems.service.dto.AlertEventDTO;
import com.vht.ems.service.dto.AlertRuleDTO;
import com.vht.ems.service.dto.DeviceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AlertEvent} and its DTO {@link AlertEventDTO}.
 */
@Mapper(componentModel = "spring")
public interface AlertEventMapper extends EntityMapper<AlertEventDTO, AlertEvent> {
    @Mapping(target = "device", source = "device", qualifiedByName = "deviceName")
    @Mapping(target = "rule", source = "rule", qualifiedByName = "alertRuleMetricType")
    AlertEventDTO toDto(AlertEvent s);

    @Named("deviceName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    DeviceDTO toDtoDeviceName(Device device);

    @Named("alertRuleMetricType")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "metricType", source = "metricType")
    AlertRuleDTO toDtoAlertRuleMetricType(AlertRule alertRule);
}
