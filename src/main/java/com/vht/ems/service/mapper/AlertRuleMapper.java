package com.vht.ems.service.mapper;

import com.vht.ems.domain.AlertRule;
import com.vht.ems.service.dto.AlertRuleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AlertRule} and its DTO {@link AlertRuleDTO}.
 */
@Mapper(componentModel = "spring")
public interface AlertRuleMapper extends EntityMapper<AlertRuleDTO, AlertRule> {}
