package com.vht.ems.web.rest;

import com.vht.ems.repository.AlertEventRepository;
import com.vht.ems.service.AlertEventService;
import com.vht.ems.service.dto.AlertEventDTO;
import com.vht.ems.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
     * {@code POST  /alert-events} : Create a new alertEvent.
     *
     * @param alertEventDTO the alertEventDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alertEventDTO, or with status {@code 400 (Bad Request)} if the alertEvent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AlertEventDTO> createAlertEvent(@Valid @RequestBody AlertEventDTO alertEventDTO) throws URISyntaxException {
        LOG.debug("REST request to save AlertEvent : {}", alertEventDTO);
        if (alertEventDTO.getId() != null) {
            throw new BadRequestAlertException("A new alertEvent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        alertEventDTO = alertEventService.save(alertEventDTO);
        return ResponseEntity.created(new URI("/api/alert-events/" + alertEventDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, alertEventDTO.getId()))
            .body(alertEventDTO);
    }

    /**
     * {@code PUT  /alert-events/:id} : Updates an existing alertEvent.
     *
     * @param id the id of the alertEventDTO to save.
     * @param alertEventDTO the alertEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alertEventDTO,
     * or with status {@code 400 (Bad Request)} if the alertEventDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alertEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlertEventDTO> updateAlertEvent(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody AlertEventDTO alertEventDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AlertEvent : {}, {}", id, alertEventDTO);
        if (alertEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alertEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alertEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        alertEventDTO = alertEventService.update(alertEventDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, alertEventDTO.getId()))
            .body(alertEventDTO);
    }

    /**
     * {@code PATCH  /alert-events/:id} : Partial updates given fields of an existing alertEvent, field will ignore if it is null
     *
     * @param id the id of the alertEventDTO to save.
     * @param alertEventDTO the alertEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alertEventDTO,
     * or with status {@code 400 (Bad Request)} if the alertEventDTO is not valid,
     * or with status {@code 404 (Not Found)} if the alertEventDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the alertEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlertEventDTO> partialUpdateAlertEvent(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody AlertEventDTO alertEventDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AlertEvent partially : {}, {}", id, alertEventDTO);
        if (alertEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alertEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alertEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlertEventDTO> result = alertEventService.partialUpdate(alertEventDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, alertEventDTO.getId())
        );
    }

    /**
     * {@code GET  /alert-events} : get all the Alert Events.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Alert Events in body.
     */
    @GetMapping("")
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
    public ResponseEntity<AlertEventDTO> getAlertEvent(@PathVariable("id") String id) {
        LOG.debug("REST request to get AlertEvent : {}", id);
        Optional<AlertEventDTO> alertEventDTO = alertEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(alertEventDTO);
    }

    /**
     * {@code DELETE  /alert-events/:id} : delete the "id" alertEvent.
     *
     * @param id the id of the alertEventDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlertEvent(@PathVariable("id") String id) {
        LOG.debug("REST request to delete AlertEvent : {}", id);
        alertEventService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
