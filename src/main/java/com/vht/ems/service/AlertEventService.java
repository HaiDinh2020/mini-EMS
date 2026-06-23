package com.vht.ems.service;

import com.vht.ems.service.dto.AlertEventDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.vht.ems.domain.AlertEvent}.
 */
public interface AlertEventService {
    /**
     * Save a alertEvent.
     *
     * @param alertEventDTO the entity to save.
     * @return the persisted entity.
     */
    AlertEventDTO save(AlertEventDTO alertEventDTO);

    /**
     * Updates a alertEvent.
     *
     * @param alertEventDTO the entity to update.
     * @return the persisted entity.
     */
    AlertEventDTO update(AlertEventDTO alertEventDTO);

    /**
     * Partially updates a alertEvent.
     *
     * @param alertEventDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AlertEventDTO> partialUpdate(AlertEventDTO alertEventDTO);

    /**
     * Get all the alertEvents.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AlertEventDTO> findAll(Pageable pageable);

    /**
     * Get all the alertEvents with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AlertEventDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" alertEvent.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AlertEventDTO> findOne(String id);

    /**
     * Acknowledge an alert event (OPEN → ACKNOWLEDGED).
     *
     * @param id the id of the entity.
     * @return the updated entity, or empty if not found or not in OPEN state.
     */
    Optional<AlertEventDTO> acknowledge(String id);

    /**
     * Delete the "id" alertEvent.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
