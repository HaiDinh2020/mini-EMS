package com.vht.ems.service.impl;

import com.vht.ems.domain.AlertEvent;
import com.vht.ems.repository.AlertEventRepository;
import com.vht.ems.service.AlertEventService;
import com.vht.ems.service.dto.AlertEventDTO;
import com.vht.ems.service.mapper.AlertEventMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.vht.ems.domain.AlertEvent}.
 */
@Service
public class AlertEventServiceImpl implements AlertEventService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertEventServiceImpl.class);

    private final AlertEventRepository alertEventRepository;

    private final AlertEventMapper alertEventMapper;

    public AlertEventServiceImpl(AlertEventRepository alertEventRepository, AlertEventMapper alertEventMapper) {
        this.alertEventRepository = alertEventRepository;
        this.alertEventMapper = alertEventMapper;
    }

    @Override
    public AlertEventDTO save(AlertEventDTO alertEventDTO) {
        LOG.debug("Request to save AlertEvent : {}", alertEventDTO);
        AlertEvent alertEvent = alertEventMapper.toEntity(alertEventDTO);
        alertEvent = alertEventRepository.save(alertEvent);
        return alertEventMapper.toDto(alertEvent);
    }

    @Override
    public AlertEventDTO update(AlertEventDTO alertEventDTO) {
        LOG.debug("Request to update AlertEvent : {}", alertEventDTO);
        AlertEvent alertEvent = alertEventMapper.toEntity(alertEventDTO);
        alertEvent = alertEventRepository.save(alertEvent);
        return alertEventMapper.toDto(alertEvent);
    }

    @Override
    public Optional<AlertEventDTO> partialUpdate(AlertEventDTO alertEventDTO) {
        LOG.debug("Request to partially update AlertEvent : {}", alertEventDTO);

        return alertEventRepository
            .findById(alertEventDTO.getId())
            .map(existingAlertEvent -> {
                alertEventMapper.partialUpdate(existingAlertEvent, alertEventDTO);

                return existingAlertEvent;
            })
            .map(alertEventRepository::save)
            .map(alertEventMapper::toDto);
    }

    @Override
    public Page<AlertEventDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all AlertEvents");
        return alertEventRepository.findAll(pageable).map(alertEventMapper::toDto);
    }

    public Page<AlertEventDTO> findAllWithEagerRelationships(Pageable pageable) {
        return alertEventRepository.findAllWithEagerRelationships(pageable).map(alertEventMapper::toDto);
    }

    @Override
    public Optional<AlertEventDTO> findOne(String id) {
        LOG.debug("Request to get AlertEvent : {}", id);
        return alertEventRepository.findOneWithEagerRelationships(id).map(alertEventMapper::toDto);
    }

    @Override
    public Optional<AlertEventDTO> acknowledge(String id) {
        LOG.debug("Request to acknowledge AlertEvent : {}", id);
        return alertEventRepository
            .findById(id)
            .filter(e -> e.getStatus() == com.vht.ems.domain.enumeration.AlertStatus.OPEN)
            .map(e -> {
                e.setStatus(com.vht.ems.domain.enumeration.AlertStatus.ACKNOWLEDGED);
                return alertEventRepository.save(e);
            })
            .map(alertEventMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete AlertEvent : {}", id);
        alertEventRepository.deleteById(id);
    }
}
