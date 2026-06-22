package com.vht.ems.service;

import com.vht.ems.service.dto.AlertRuleDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.vht.ems.domain.AlertRule}.
 */
public interface AlertRuleService {
    /**
     * Save a alertRule.
     *
     * @param alertRuleDTO the entity to save.
     * @return the persisted entity.
     */
    AlertRuleDTO save(AlertRuleDTO alertRuleDTO);

    /**
     * Updates a alertRule.
     *
     * @param alertRuleDTO the entity to update.
     * @return the persisted entity.
     */
    AlertRuleDTO update(AlertRuleDTO alertRuleDTO);

    /**
     * Partially updates a alertRule.
     *
     * @param alertRuleDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AlertRuleDTO> partialUpdate(AlertRuleDTO alertRuleDTO);

    /**
     * Get all the alertRules.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AlertRuleDTO> findAll(Pageable pageable);

    /**
     * Get the "id" alertRule.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AlertRuleDTO> findOne(String id);

    /**
     * Delete the "id" alertRule.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
