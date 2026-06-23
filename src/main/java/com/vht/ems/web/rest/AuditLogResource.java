package com.vht.ems.web.rest;

import com.vht.ems.security.AuthoritiesConstants;
import com.vht.ems.service.AuditLogService;
import com.vht.ems.service.dto.AuditLogDTO;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for reading Audit Logs (append-only, Admin only).
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
public class AuditLogResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogResource.class);

    private final AuditLogService auditLogService;

    public AuditLogResource(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * {@code GET  /api/admin/audit-logs} : get audit logs with optional filters.
     *
     * @param username   filter by username (optional)
     * @param action     filter by action CREATE/UPDATE/DELETE (optional)
     * @param entityName filter by entity name (optional)
     * @param from       filter from timestamp ISO-8601 (optional)
     * @param to         filter to timestamp ISO-8601 (optional)
     * @param pageable   the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of audit logs in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String entityName,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get AuditLogs with filters: username={}, action={}, entityName={}", username, action, entityName);
        Page<AuditLogDTO> page = auditLogService.findWithFilters(username, action, entityName, from, to, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /api/admin/audit-logs/:id} : get the "id" auditLog.
     *
     * @param id the id of the auditLogDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auditLogDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDTO> getAuditLog(@PathVariable("id") String id) {
        LOG.debug("REST request to get AuditLog : {}", id);
        Optional<AuditLogDTO> auditLogDTO = auditLogService.findOne(id);
        return ResponseUtil.wrapOrNotFound(auditLogDTO);
    }
}
