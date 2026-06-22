package com.vht.ems.service.impl;

import com.vht.ems.domain.Credential;
import com.vht.ems.repository.CredentialRepository;
import com.vht.ems.service.CredentialService;
import com.vht.ems.service.dto.CredentialDTO;
import com.vht.ems.service.mapper.CredentialMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.vht.ems.domain.Credential}.
 */
@Service
public class CredentialServiceImpl implements CredentialService {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialServiceImpl.class);

    private final CredentialRepository credentialRepository;

    private final CredentialMapper credentialMapper;

    public CredentialServiceImpl(CredentialRepository credentialRepository, CredentialMapper credentialMapper) {
        this.credentialRepository = credentialRepository;
        this.credentialMapper = credentialMapper;
    }

    @Override
    public CredentialDTO save(CredentialDTO credentialDTO) {
        LOG.debug("Request to save Credential : {}", credentialDTO);
        Credential credential = credentialMapper.toEntity(credentialDTO);
        credential = credentialRepository.save(credential);
        return credentialMapper.toDto(credential);
    }

    @Override
    public CredentialDTO update(CredentialDTO credentialDTO) {
        LOG.debug("Request to update Credential : {}", credentialDTO);
        Credential credential = credentialMapper.toEntity(credentialDTO);
        credential = credentialRepository.save(credential);
        return credentialMapper.toDto(credential);
    }

    @Override
    public Optional<CredentialDTO> partialUpdate(CredentialDTO credentialDTO) {
        LOG.debug("Request to partially update Credential : {}", credentialDTO);

        return credentialRepository
            .findById(credentialDTO.getId())
            .map(existingCredential -> {
                credentialMapper.partialUpdate(existingCredential, credentialDTO);

                return existingCredential;
            })
            .map(credentialRepository::save)
            .map(credentialMapper::toDto);
    }

    @Override
    public Page<CredentialDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Credentials");
        return credentialRepository.findAll(pageable).map(credentialMapper::toDto);
    }

    @Override
    public Optional<CredentialDTO> findOne(String id) {
        LOG.debug("Request to get Credential : {}", id);
        return credentialRepository.findById(id).map(credentialMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Credential : {}", id);
        credentialRepository.deleteById(id);
    }
}
