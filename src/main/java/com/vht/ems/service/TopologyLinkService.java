package com.vht.ems.service;

import com.vht.ems.service.dto.TopologyLinkDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.vht.ems.domain.TopologyLink}.
 */
public interface TopologyLinkService {
    /**
     * Save a topologyLink.
     *
     * @param topologyLinkDTO the entity to save.
     * @return the persisted entity.
     */
    TopologyLinkDTO save(TopologyLinkDTO topologyLinkDTO);

    /**
     * Updates a topologyLink.
     *
     * @param topologyLinkDTO the entity to update.
     * @return the persisted entity.
     */
    TopologyLinkDTO update(TopologyLinkDTO topologyLinkDTO);

    /**
     * Partially updates a topologyLink.
     *
     * @param topologyLinkDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TopologyLinkDTO> partialUpdate(TopologyLinkDTO topologyLinkDTO);

    /**
     * Get all the topologyLinks.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TopologyLinkDTO> findAll(Pageable pageable);

    /**
     * Get all the topologyLinks with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TopologyLinkDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" topologyLink.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TopologyLinkDTO> findOne(String id);

    /**
     * Delete the "id" topologyLink.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
