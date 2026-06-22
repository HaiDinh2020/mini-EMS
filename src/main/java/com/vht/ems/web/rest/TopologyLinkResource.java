package com.vht.ems.web.rest;

import com.vht.ems.repository.TopologyLinkRepository;
import com.vht.ems.service.TopologyLinkService;
import com.vht.ems.service.dto.TopologyLinkDTO;
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
 * REST controller for managing {@link com.vht.ems.domain.TopologyLink}.
 */
@RestController
@RequestMapping("/api/topology-links")
public class TopologyLinkResource {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyLinkResource.class);

    private static final String ENTITY_NAME = "topologyLink";

    @Value("${jhipster.clientApp.name:eMS}")
    private String applicationName;

    private final TopologyLinkService topologyLinkService;

    private final TopologyLinkRepository topologyLinkRepository;

    public TopologyLinkResource(TopologyLinkService topologyLinkService, TopologyLinkRepository topologyLinkRepository) {
        this.topologyLinkService = topologyLinkService;
        this.topologyLinkRepository = topologyLinkRepository;
    }

    /**
     * {@code POST  /topology-links} : Create a new topologyLink.
     *
     * @param topologyLinkDTO the topologyLinkDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new topologyLinkDTO, or with status {@code 400 (Bad Request)} if the topologyLink has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TopologyLinkDTO> createTopologyLink(@Valid @RequestBody TopologyLinkDTO topologyLinkDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TopologyLink : {}", topologyLinkDTO);
        if (topologyLinkDTO.getId() != null) {
            throw new BadRequestAlertException("A new topologyLink cannot already have an ID", ENTITY_NAME, "idexists");
        }
        topologyLinkDTO = topologyLinkService.save(topologyLinkDTO);
        return ResponseEntity.created(new URI("/api/topology-links/" + topologyLinkDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, topologyLinkDTO.getId()))
            .body(topologyLinkDTO);
    }

    /**
     * {@code PUT  /topology-links/:id} : Updates an existing topologyLink.
     *
     * @param id the id of the topologyLinkDTO to save.
     * @param topologyLinkDTO the topologyLinkDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated topologyLinkDTO,
     * or with status {@code 400 (Bad Request)} if the topologyLinkDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the topologyLinkDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TopologyLinkDTO> updateTopologyLink(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody TopologyLinkDTO topologyLinkDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TopologyLink : {}, {}", id, topologyLinkDTO);
        if (topologyLinkDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, topologyLinkDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!topologyLinkRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        topologyLinkDTO = topologyLinkService.update(topologyLinkDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, topologyLinkDTO.getId()))
            .body(topologyLinkDTO);
    }

    /**
     * {@code PATCH  /topology-links/:id} : Partial updates given fields of an existing topologyLink, field will ignore if it is null
     *
     * @param id the id of the topologyLinkDTO to save.
     * @param topologyLinkDTO the topologyLinkDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated topologyLinkDTO,
     * or with status {@code 400 (Bad Request)} if the topologyLinkDTO is not valid,
     * or with status {@code 404 (Not Found)} if the topologyLinkDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the topologyLinkDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TopologyLinkDTO> partialUpdateTopologyLink(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody TopologyLinkDTO topologyLinkDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TopologyLink partially : {}, {}", id, topologyLinkDTO);
        if (topologyLinkDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, topologyLinkDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!topologyLinkRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TopologyLinkDTO> result = topologyLinkService.partialUpdate(topologyLinkDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, topologyLinkDTO.getId())
        );
    }

    /**
     * {@code GET  /topology-links} : get all the Topology Links.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Topology Links in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TopologyLinkDTO>> getAllTopologyLinks(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of TopologyLinks");
        Page<TopologyLinkDTO> page;
        if (eagerload) {
            page = topologyLinkService.findAllWithEagerRelationships(pageable);
        } else {
            page = topologyLinkService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /topology-links/:id} : get the "id" topologyLink.
     *
     * @param id the id of the topologyLinkDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the topologyLinkDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TopologyLinkDTO> getTopologyLink(@PathVariable("id") String id) {
        LOG.debug("REST request to get TopologyLink : {}", id);
        Optional<TopologyLinkDTO> topologyLinkDTO = topologyLinkService.findOne(id);
        return ResponseUtil.wrapOrNotFound(topologyLinkDTO);
    }

    /**
     * {@code DELETE  /topology-links/:id} : delete the "id" topologyLink.
     *
     * @param id the id of the topologyLinkDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopologyLink(@PathVariable("id") String id) {
        LOG.debug("REST request to delete TopologyLink : {}", id);
        topologyLinkService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
