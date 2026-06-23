package com.vht.ems.web.rest;

import com.vht.ems.repository.AlertRuleRepository;
import com.vht.ems.security.AuthoritiesConstants;
import com.vht.ems.service.AlertRuleService;
import com.vht.ems.service.dto.AlertRuleDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.vht.ems.domain.AlertRule}.
 */
@RestController
@RequestMapping("/api/alert-rules")
public class AlertRuleResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRuleResource.class);

    private static final String ENTITY_NAME = "alertRule";

    @Value("${jhipster.clientApp.name:eMS}")
    private String applicationName;

    private final AlertRuleService alertRuleService;

    private final AlertRuleRepository alertRuleRepository;

    public AlertRuleResource(AlertRuleService alertRuleService, AlertRuleRepository alertRuleRepository) {
        this.alertRuleService = alertRuleService;
        this.alertRuleRepository = alertRuleRepository;
    }

    /**
     * {@code POST  /alert-rules} : Create a new alertRule.
     *
     * @param alertRuleDTO the alertRuleDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alertRuleDTO, or with status {@code 400 (Bad Request)} if the alertRule has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AlertRuleDTO> createAlertRule(@Valid @RequestBody AlertRuleDTO alertRuleDTO) throws URISyntaxException {
        LOG.debug("REST request to save AlertRule : {}", alertRuleDTO);
        if (alertRuleDTO.getId() != null) {
            throw new BadRequestAlertException("A new alertRule cannot already have an ID", ENTITY_NAME, "idexists");
        }
        alertRuleDTO = alertRuleService.save(alertRuleDTO);
        return ResponseEntity.created(new URI("/api/alert-rules/" + alertRuleDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, alertRuleDTO.getId()))
            .body(alertRuleDTO);
    }

    /**
     * {@code PUT  /alert-rules/:id} : Updates an existing alertRule.
     *
     * @param id the id of the alertRuleDTO to save.
     * @param alertRuleDTO the alertRuleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alertRuleDTO,
     * or with status {@code 400 (Bad Request)} if the alertRuleDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alertRuleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AlertRuleDTO> updateAlertRule(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody AlertRuleDTO alertRuleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AlertRule : {}, {}", id, alertRuleDTO);
        if (alertRuleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alertRuleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alertRuleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        alertRuleDTO = alertRuleService.update(alertRuleDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, alertRuleDTO.getId()))
            .body(alertRuleDTO);
    }

    /**
     * {@code PATCH  /alert-rules/:id} : Partial updates given fields of an existing alertRule, field will ignore if it is null
     *
     * @param id the id of the alertRuleDTO to save.
     * @param alertRuleDTO the alertRuleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alertRuleDTO,
     * or with status {@code 400 (Bad Request)} if the alertRuleDTO is not valid,
     * or with status {@code 404 (Not Found)} if the alertRuleDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the alertRuleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AlertRuleDTO> partialUpdateAlertRule(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody AlertRuleDTO alertRuleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AlertRule partially : {}, {}", id, alertRuleDTO);
        if (alertRuleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alertRuleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alertRuleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlertRuleDTO> result = alertRuleService.partialUpdate(alertRuleDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, alertRuleDTO.getId())
        );
    }

    /**
     * {@code GET  /alert-rules} : get all the Alert Rules.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Alert Rules in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<List<AlertRuleDTO>> getAllAlertRules(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of AlertRules");
        Page<AlertRuleDTO> page = alertRuleService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /alert-rules/:id} : get the "id" alertRule.
     *
     * @param id the id of the alertRuleDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alertRuleDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<AlertRuleDTO> getAlertRule(@PathVariable("id") String id) {
        LOG.debug("REST request to get AlertRule : {}", id);
        Optional<AlertRuleDTO> alertRuleDTO = alertRuleService.findOne(id);
        return ResponseUtil.wrapOrNotFound(alertRuleDTO);
    }

    /**
     * {@code DELETE  /alert-rules/:id} : delete the "id" alertRule.
     *
     * @param id the id of the alertRuleDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteAlertRule(@PathVariable("id") String id) {
        LOG.debug("REST request to delete AlertRule : {}", id);
        alertRuleService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
