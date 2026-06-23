package com.vht.ems.web.rest;

import com.vht.ems.repository.AlertEventRepository;
import com.vht.ems.security.AuthoritiesConstants;
import com.vht.ems.service.AlertEventService;
import com.vht.ems.service.dto.AlertEventDTO;
import com.vht.ems.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.vht.ems.domain.AlertEvent}.
 */
@RestController
@RequestMapping("/api/alert-events")
public class AlertEventResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlertEventResource.class);

    private static final String ENTITY_NAME = "alertEvent";

    @Value("${jhipster.clientApp.name:eMS}")
    private String applicationName;

    private final AlertEventService alertEventService;

    private final AlertEventRepository alertEventRepository;

    public AlertEventResource(AlertEventService alertEventService, AlertEventRepository alertEventRepository) {
        this.alertEventService = alertEventService;
        this.alertEventRepository = alertEventRepository;
    }

    /**
     * {@code GET  /alert-events} : get all the Alert Events.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Alert Events in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<List<AlertEventDTO>> getAllAlertEvents(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of AlertEvents");
        Page<AlertEventDTO> page;
        if (eagerload) {
            page = alertEventService.findAllWithEagerRelationships(pageable);
        } else {
            page = alertEventService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /alert-events/:id} : get the "id" alertEvent.
     *
     * @param id the id of the alertEventDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alertEventDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<AlertEventDTO> getAlertEvent(@PathVariable("id") String id) {
        LOG.debug("REST request to get AlertEvent : {}", id);
        Optional<AlertEventDTO> alertEventDTO = alertEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(alertEventDTO);
    }

    /**
     * {@code PUT  /alert-events/:id/acknowledge} : Acknowledge an OPEN alert event.
     *
     * @param id the id of the alertEventDTO to acknowledge.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and updated alertEventDTO, or {@code 404 (Not Found)}.
     */
    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<AlertEventDTO> acknowledgeAlertEvent(@PathVariable("id") String id) {
        LOG.debug("REST request to acknowledge AlertEvent : {}", id);
        if (!alertEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        Optional<AlertEventDTO> result = alertEventService.acknowledge(id);
        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id));
    }

    /**
     * {@code DELETE  /alert-events/:id} : delete the "id" alertEvent (Admin only).
     *
     * @param id the id of the alertEventDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteAlertEvent(@PathVariable("id") String id) {
        LOG.debug("REST request to delete AlertEvent : {}", id);
        alertEventService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
