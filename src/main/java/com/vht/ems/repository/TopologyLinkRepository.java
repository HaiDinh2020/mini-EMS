package com.vht.ems.repository;

import com.vht.ems.domain.TopologyLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the TopologyLink entity.
 */
@Repository
public interface TopologyLinkRepository extends MongoRepository<TopologyLink, String> {
    @Query("{}")
    Page<TopologyLink> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<TopologyLink> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<TopologyLink> findOneWithEagerRelationships(String id);
}
