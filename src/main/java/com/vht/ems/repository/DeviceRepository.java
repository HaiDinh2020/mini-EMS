package com.vht.ems.repository;

import com.vht.ems.domain.Device;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Device entity.
 */
@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {
    @Query("{}")
    Page<Device> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<Device> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<Device> findOneWithEagerRelationships(String id);

    boolean existsByIpAddress(String ipAddress);
}
