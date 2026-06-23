package com.vht.ems.repository;

import com.vht.ems.domain.AlertRule;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the AlertRule entity.
 */
@Repository
public interface AlertRuleRepository extends MongoRepository<AlertRule, String> {
    /**
     * Find all enabled rules that apply to the given devices (device-specific or global).
     */
    @Query("{ 'enabled': true, $or: [ { 'device_id': { $in: ?0 } }, { 'device_id': null } ] }")
    List<AlertRule> findEnabledRulesForDevices(List<String> deviceIds);

    /**
     * Find all globally enabled rules (no device filter).
     */
    List<AlertRule> findByEnabledTrue();
}
