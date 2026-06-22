package com.vht.ems.repository;

import com.vht.ems.domain.MetricSample;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the MetricSample entity.
 */
@Repository
public interface MetricSampleRepository extends MongoRepository<MetricSample, String> {
    @Query("{}")
    Page<MetricSample> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<MetricSample> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<MetricSample> findOneWithEagerRelationships(String id);
}
