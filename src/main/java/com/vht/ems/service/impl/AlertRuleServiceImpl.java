package com.vht.ems.service.impl;

import com.vht.ems.domain.AlertRule;
import com.vht.ems.repository.AlertRuleRepository;
import com.vht.ems.service.AlertRuleService;
import com.vht.ems.service.dto.AlertRuleDTO;
import com.vht.ems.service.mapper.AlertRuleMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.vht.ems.domain.AlertRule}.
 */
@Service
public class AlertRuleServiceImpl implements AlertRuleService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRuleServiceImpl.class);

    private final AlertRuleRepository alertRuleRepository;

    private final AlertRuleMapper alertRuleMapper;

    public AlertRuleServiceImpl(AlertRuleRepository alertRuleRepository, AlertRuleMapper alertRuleMapper) {
        this.alertRuleRepository = alertRuleRepository;
        this.alertRuleMapper = alertRuleMapper;
    }

    @Override
    public AlertRuleDTO save(AlertRuleDTO alertRuleDTO) {
        LOG.debug("Request to save AlertRule : {}", alertRuleDTO);
        AlertRule alertRule = alertRuleMapper.toEntity(alertRuleDTO);
        alertRule = alertRuleRepository.save(alertRule);
        return alertRuleMapper.toDto(alertRule);
    }

    @Override
    public AlertRuleDTO update(AlertRuleDTO alertRuleDTO) {
        LOG.debug("Request to update AlertRule : {}", alertRuleDTO);
        AlertRule alertRule = alertRuleMapper.toEntity(alertRuleDTO);
        alertRule = alertRuleRepository.save(alertRule);
        return alertRuleMapper.toDto(alertRule);
    }

    @Override
    public Optional<AlertRuleDTO> partialUpdate(AlertRuleDTO alertRuleDTO) {
        LOG.debug("Request to partially update AlertRule : {}", alertRuleDTO);

        return alertRuleRepository
            .findById(alertRuleDTO.getId())
            .map(existingAlertRule -> {
                alertRuleMapper.partialUpdate(existingAlertRule, alertRuleDTO);

                return existingAlertRule;
            })
            .map(alertRuleRepository::save)
            .map(alertRuleMapper::toDto);
    }

    @Override
    public Page<AlertRuleDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all AlertRules");
        return alertRuleRepository.findAll(pageable).map(alertRuleMapper::toDto);
    }

    @Override
    public Optional<AlertRuleDTO> findOne(String id) {
        LOG.debug("Request to get AlertRule : {}", id);
        return alertRuleRepository.findById(id).map(alertRuleMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete AlertRule : {}", id);
        alertRuleRepository.deleteById(id);
    }
}
