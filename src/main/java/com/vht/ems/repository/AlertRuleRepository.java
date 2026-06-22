package com.vht.ems.repository;

import com.vht.ems.domain.AlertRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the AlertRule entity.
 */
@Repository
public interface AlertRuleRepository extends MongoRepository<AlertRule, String> {}
