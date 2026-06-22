package com.vht.ems.service;

import com.vht.ems.service.dto.MetricSampleDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.vht.ems.domain.MetricSample}.
 */
public interface MetricSampleService {
    /**
     * Save a metricSample.
     *
     * @param metricSampleDTO the entity to save.
     * @return the persisted entity.
     */
    MetricSampleDTO save(MetricSampleDTO metricSampleDTO);

    /**
     * Updates a metricSample.
     *
     * @param metricSampleDTO the entity to update.
     * @return the persisted entity.
     */
    MetricSampleDTO update(MetricSampleDTO metricSampleDTO);

    /**
     * Partially updates a metricSample.
     *
     * @param metricSampleDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<MetricSampleDTO> partialUpdate(MetricSampleDTO metricSampleDTO);

    /**
     * Get all the metricSamples.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MetricSampleDTO> findAll(Pageable pageable);

    /**
     * Get all the metricSamples with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MetricSampleDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" metricSample.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<MetricSampleDTO> findOne(String id);

    /**
     * Delete the "id" metricSample.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
