package com.vht.ems.web.rest;

import com.vht.ems.repository.MetricSampleRepository;
import com.vht.ems.service.MetricSampleService;
import com.vht.ems.service.dto.MetricSampleDTO;
import com.vht.ems.service.mapper.MetricSampleMapper;
import com.vht.ems.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.vht.ems.domain.MetricSample}.
 */
@RestController
@RequestMapping("/api/metric-samples")
public class MetricSampleResource {

    private static final Logger LOG = LoggerFactory.getLogger(MetricSampleResource.class);

    private static final String ENTITY_NAME = "metricSample";

    @Value("${jhipster.clientApp.name:eMS}")
    private String applicationName;

    private final MetricSampleService metricSampleService;

    private final MetricSampleRepository metricSampleRepository;

    private final MetricSampleMapper metricSampleMapper;

    public MetricSampleResource(
        MetricSampleService metricSampleService,
        MetricSampleRepository metricSampleRepository,
        MetricSampleMapper metricSampleMapper
    ) {
        this.metricSampleService = metricSampleService;
        this.metricSampleRepository = metricSampleRepository;
        this.metricSampleMapper = metricSampleMapper;
    }

    /**
     * {@code POST  /metric-samples} : Create a new metricSample.
     *
     * @param metricSampleDTO the metricSampleDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new metricSampleDTO, or with status {@code 400 (Bad Request)} if the metricSample has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MetricSampleDTO> createMetricSample(@Valid @RequestBody MetricSampleDTO metricSampleDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save MetricSample : {}", metricSampleDTO);
        if (metricSampleDTO.getId() != null) {
            throw new BadRequestAlertException("A new metricSample cannot already have an ID", ENTITY_NAME, "idexists");
        }
        metricSampleDTO = metricSampleService.save(metricSampleDTO);
        return ResponseEntity.created(new URI("/api/metric-samples/" + metricSampleDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, metricSampleDTO.getId()))
            .body(metricSampleDTO);
    }

    /**
     * {@code PUT  /metric-samples/:id} : Updates an existing metricSample.
     *
     * @param id the id of the metricSampleDTO to save.
     * @param metricSampleDTO the metricSampleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated metricSampleDTO,
     * or with status {@code 400 (Bad Request)} if the metricSampleDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the metricSampleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MetricSampleDTO> updateMetricSample(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody MetricSampleDTO metricSampleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MetricSample : {}, {}", id, metricSampleDTO);
        if (metricSampleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, metricSampleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!metricSampleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        metricSampleDTO = metricSampleService.update(metricSampleDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, metricSampleDTO.getId()))
            .body(metricSampleDTO);
    }

    /**
     * {@code PATCH  /metric-samples/:id} : Partial updates given fields of an existing metricSample, field will ignore if it is null
     *
     * @param id the id of the metricSampleDTO to save.
     * @param metricSampleDTO the metricSampleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated metricSampleDTO,
     * or with status {@code 400 (Bad Request)} if the metricSampleDTO is not valid,
     * or with status {@code 404 (Not Found)} if the metricSampleDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the metricSampleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MetricSampleDTO> partialUpdateMetricSample(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody MetricSampleDTO metricSampleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MetricSample partially : {}, {}", id, metricSampleDTO);
        if (metricSampleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, metricSampleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!metricSampleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MetricSampleDTO> result = metricSampleService.partialUpdate(metricSampleDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, metricSampleDTO.getId())
        );
    }

    /**
     * {@code GET  /metric-samples} : get all the Metric Samples.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Metric Samples in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MetricSampleDTO>> getAllMetricSamples(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of MetricSamples");
        Page<MetricSampleDTO> page;
        if (eagerload) {
            page = metricSampleService.findAllWithEagerRelationships(pageable);
        } else {
            page = metricSampleService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /metric-samples/:id} : get the "id" metricSample.
     *
     * @param id the id of the metricSampleDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the metricSampleDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MetricSampleDTO> getMetricSample(@PathVariable("id") String id) {
        LOG.debug("REST request to get MetricSample : {}", id);
        Optional<MetricSampleDTO> metricSampleDTO = metricSampleService.findOne(id);
        return ResponseUtil.wrapOrNotFound(metricSampleDTO);
    }

    /**
     * {@code DELETE  /metric-samples/:id} : delete the "id" metricSample.
     *
     * @param id the id of the metricSampleDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetricSample(@PathVariable("id") String id) {
        LOG.debug("REST request to delete MetricSample : {}", id);
        metricSampleService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }

    // ---- device-scoped metric endpoints ----

    /**
     * {@code GET  /devices/:deviceId/metrics} : paginated metric history for a device.
     */
    @GetMapping("/devices/{deviceId}/metrics")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<MetricSampleDTO>> getDeviceMetrics(
        @PathVariable String deviceId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get metrics for device {}", deviceId);
        Page<MetricSampleDTO> page = metricSampleRepository
            .findByDeviceIdOrderByCollectedAtDesc(deviceId, pageable)
            .map(metricSampleMapper::toDto);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /devices/:deviceId/metrics/latest} : the most recent MetricSample for a device.
     */
    @GetMapping("/devices/{deviceId}/metrics/latest")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<MetricSampleDTO> getLatestDeviceMetric(@PathVariable String deviceId) {
        LOG.debug("REST request to get latest metric for device {}", deviceId);
        return ResponseUtil.wrapOrNotFound(
            metricSampleRepository.findTopByDeviceIdOrderByCollectedAtDesc(deviceId).map(metricSampleMapper::toDto)
        );
    }

    /**
     * {@code GET  /devices/:deviceId/metrics/history} : time-range metric history for a device.
     *
     * @param deviceId  the device ID
     * @param from      start of the time window (ISO-8601, default = now minus 1 hour)
     * @param to        end of the time window   (ISO-8601, default = now)
     * @param limit     max number of samples to return (default 50)
     */
    @GetMapping("/devices/{deviceId}/metrics/history")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<MetricSampleDTO>> getDeviceMetricHistory(
        @PathVariable String deviceId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam(defaultValue = "50") int limit
    ) {
        LOG.debug("REST request to get metric history for device {}, from={}, to={}, limit={}", deviceId, from, to, limit);
        Instant effectiveTo = (to != null) ? to : Instant.now();
        Instant effectiveFrom = (from != null) ? from : effectiveTo.minus(1, ChronoUnit.HOURS);
        List<MetricSampleDTO> result = metricSampleRepository
            .findByDeviceIdAndCollectedAtBetweenOrderByCollectedAtAsc(deviceId, effectiveFrom, effectiveTo)
            .stream()
            .limit(Math.min(limit, 500))
            .map(metricSampleMapper::toDto)
            .toList();
        return ResponseEntity.ok(result);
    }
}
