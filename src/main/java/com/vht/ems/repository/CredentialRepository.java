package com.vht.ems.repository;

import com.vht.ems.domain.Credential;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Credential entity.
 */
@Repository
public interface CredentialRepository extends MongoRepository<Credential, String> {}
