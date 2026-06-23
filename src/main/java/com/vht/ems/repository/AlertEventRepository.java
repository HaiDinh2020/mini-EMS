package com.vht.ems.repository;

import com.vht.ems.domain.AlertEvent;
import com.vht.ems.domain.AlertRule;
import com.vht.ems.domain.Device;
import com.vht.ems.domain.enumeration.AlertStatus;
import com.vht.ems.domain.enumeration.Severity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the AlertEvent entity.
 */
@Repository
public interface AlertEventRepository extends MongoRepository<AlertEvent, String> {
    @Query("{}")
    Page<AlertEvent> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<AlertEvent> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<AlertEvent> findOneWithEagerRelationships(String id);

    Optional<AlertEvent> findByDeviceAndRuleAndStatus(Device device, AlertRule rule, AlertStatus status);

    List<AlertEvent> findByDeviceAndStatus(Device device, AlertStatus status);

    long countByStatusAndSeverity(AlertStatus status, Severity severity);

    Page<AlertEvent> findByStatus(AlertStatus status, Pageable pageable);

    Page<AlertEvent> findBySeverity(Severity severity, Pageable pageable);

    Page<AlertEvent> findByStatusAndSeverity(AlertStatus status, Severity severity, Pageable pageable);
}
