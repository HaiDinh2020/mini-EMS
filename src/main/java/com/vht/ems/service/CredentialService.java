package com.vht.ems.service;

import com.vht.ems.service.dto.CredentialDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.vht.ems.domain.Credential}.
 */
public interface CredentialService {
    /**
     * Save a credential.
     *
     * @param credentialDTO the entity to save.
     * @return the persisted entity.
     */
    CredentialDTO save(CredentialDTO credentialDTO);

    /**
     * Updates a credential.
     *
     * @param credentialDTO the entity to update.
     * @return the persisted entity.
     */
    CredentialDTO update(CredentialDTO credentialDTO);

    /**
     * Partially updates a credential.
     *
     * @param credentialDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CredentialDTO> partialUpdate(CredentialDTO credentialDTO);

    /**
     * Get all the credentials.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CredentialDTO> findAll(Pageable pageable);

    /**
     * Get the "id" credential.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CredentialDTO> findOne(String id);

    /**
     * Delete the "id" credential.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
