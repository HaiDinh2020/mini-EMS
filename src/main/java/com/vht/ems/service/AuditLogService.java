package com.vht.ems.service;

import com.vht.ems.service.dto.AuditLogDTO;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.vht.ems.domain.AuditLog}.
 */
public interface AuditLogService {
    /**
     * Save a auditLog.
     *
     * @param auditLogDTO the entity to save.
     * @return the persisted entity.
     */
    AuditLogDTO save(AuditLogDTO auditLogDTO);

    /**
     * Updates a auditLog.
     *
     * @param auditLogDTO the entity to update.
     * @return the persisted entity.
     */
    AuditLogDTO update(AuditLogDTO auditLogDTO);

    /**
     * Partially updates a auditLog.
     *
     * @param auditLogDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AuditLogDTO> partialUpdate(AuditLogDTO auditLogDTO);

    /**
     * Get all the auditLogs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AuditLogDTO> findAll(Pageable pageable);

    /**
     * Get audit logs with optional filters.
     *
     * @param username filter by username (optional)
     * @param action   filter by action (optional)
     * @param entityName filter by entity name (optional)
     * @param from     filter from timestamp (optional)
     * @param to       filter to timestamp (optional)
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AuditLogDTO> findWithFilters(String username, String action, String entityName, Instant from, Instant to, Pageable pageable);

    /**
     * Get the "id" auditLog.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuditLogDTO> findOne(String id);

    /**
     * Delete the "id" auditLog.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
