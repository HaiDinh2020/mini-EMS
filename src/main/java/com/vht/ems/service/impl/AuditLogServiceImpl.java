package com.vht.ems.service.impl;

import com.vht.ems.domain.AuditLog;
import com.vht.ems.repository.AuditLogRepository;
import com.vht.ems.service.AuditLogService;
import com.vht.ems.service.dto.AuditLogDTO;
import com.vht.ems.service.mapper.AuditLogMapper;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.vht.ems.domain.AuditLog}.
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    private final MongoTemplate mongoTemplate;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper, MongoTemplate mongoTemplate) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public AuditLogDTO save(AuditLogDTO auditLogDTO) {
        LOG.debug("Request to save AuditLog : {}", auditLogDTO);
        AuditLog auditLog = auditLogMapper.toEntity(auditLogDTO);
        auditLog = auditLogRepository.save(auditLog);
        return auditLogMapper.toDto(auditLog);
    }

    @Override
    public AuditLogDTO update(AuditLogDTO auditLogDTO) {
        LOG.debug("Request to update AuditLog : {}", auditLogDTO);
        AuditLog auditLog = auditLogMapper.toEntity(auditLogDTO);
        auditLog = auditLogRepository.save(auditLog);
        return auditLogMapper.toDto(auditLog);
    }

    @Override
    public Optional<AuditLogDTO> partialUpdate(AuditLogDTO auditLogDTO) {
        LOG.debug("Request to partially update AuditLog : {}", auditLogDTO);
        return auditLogRepository
            .findById(auditLogDTO.getId())
            .map(existingAuditLog -> {
                auditLogMapper.partialUpdate(existingAuditLog, auditLogDTO);
                return existingAuditLog;
            })
            .map(auditLogRepository::save)
            .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all AuditLogs");
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogDTO> findWithFilters(
        String username,
        String action,
        String entityName,
        Instant from,
        Instant to,
        Pageable pageable
    ) {
        LOG.debug("Request to get AuditLogs with filters");
        var criteriaList = new java.util.ArrayList<Criteria>();
        if (username != null && !username.isBlank()) criteriaList.add(Criteria.where("username").is(username));
        if (action != null && !action.isBlank()) criteriaList.add(Criteria.where("action").is(action));
        if (entityName != null && !entityName.isBlank()) criteriaList.add(Criteria.where("entity_name").is(entityName));
        if (from != null && to != null) criteriaList.add(Criteria.where("timestamp").gte(from).lte(to));
        else if (from != null) criteriaList.add(Criteria.where("timestamp").gte(from));
        else if (to != null) criteriaList.add(Criteria.where("timestamp").lte(to));

        Criteria combined = criteriaList.isEmpty() ? new Criteria() : new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));

        Query query = new Query(combined).with(pageable);
        long total = mongoTemplate.count(new Query(combined), AuditLog.class);
        var results = mongoTemplate.find(query, AuditLog.class);
        return new PageImpl<>(results.stream().map(auditLogMapper::toDto).toList(), pageable, total);
    }

    @Override
    public Optional<AuditLogDTO> findOne(String id) {
        LOG.debug("Request to get AuditLog : {}", id);
        return auditLogRepository.findById(id).map(auditLogMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete AuditLog : {}", id);
        auditLogRepository.deleteById(id);
    }
}
