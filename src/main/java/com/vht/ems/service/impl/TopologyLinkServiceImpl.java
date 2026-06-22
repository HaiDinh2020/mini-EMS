package com.vht.ems.service.impl;

import com.vht.ems.domain.TopologyLink;
import com.vht.ems.repository.TopologyLinkRepository;
import com.vht.ems.service.TopologyLinkService;
import com.vht.ems.service.dto.TopologyLinkDTO;
import com.vht.ems.service.mapper.TopologyLinkMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.vht.ems.domain.TopologyLink}.
 */
@Service
public class TopologyLinkServiceImpl implements TopologyLinkService {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyLinkServiceImpl.class);

    private final TopologyLinkRepository topologyLinkRepository;

    private final TopologyLinkMapper topologyLinkMapper;

    public TopologyLinkServiceImpl(TopologyLinkRepository topologyLinkRepository, TopologyLinkMapper topologyLinkMapper) {
        this.topologyLinkRepository = topologyLinkRepository;
        this.topologyLinkMapper = topologyLinkMapper;
    }

    @Override
    public TopologyLinkDTO save(TopologyLinkDTO topologyLinkDTO) {
        LOG.debug("Request to save TopologyLink : {}", topologyLinkDTO);
        TopologyLink topologyLink = topologyLinkMapper.toEntity(topologyLinkDTO);
        topologyLink = topologyLinkRepository.save(topologyLink);
        return topologyLinkMapper.toDto(topologyLink);
    }

    @Override
    public TopologyLinkDTO update(TopologyLinkDTO topologyLinkDTO) {
        LOG.debug("Request to update TopologyLink : {}", topologyLinkDTO);
        TopologyLink topologyLink = topologyLinkMapper.toEntity(topologyLinkDTO);
        topologyLink = topologyLinkRepository.save(topologyLink);
        return topologyLinkMapper.toDto(topologyLink);
    }

    @Override
    public Optional<TopologyLinkDTO> partialUpdate(TopologyLinkDTO topologyLinkDTO) {
        LOG.debug("Request to partially update TopologyLink : {}", topologyLinkDTO);

        return topologyLinkRepository
            .findById(topologyLinkDTO.getId())
            .map(existingTopologyLink -> {
                topologyLinkMapper.partialUpdate(existingTopologyLink, topologyLinkDTO);

                return existingTopologyLink;
            })
            .map(topologyLinkRepository::save)
            .map(topologyLinkMapper::toDto);
    }

    @Override
    public Page<TopologyLinkDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TopologyLinks");
        return topologyLinkRepository.findAll(pageable).map(topologyLinkMapper::toDto);
    }

    public Page<TopologyLinkDTO> findAllWithEagerRelationships(Pageable pageable) {
        return topologyLinkRepository.findAllWithEagerRelationships(pageable).map(topologyLinkMapper::toDto);
    }

    @Override
    public Optional<TopologyLinkDTO> findOne(String id) {
        LOG.debug("Request to get TopologyLink : {}", id);
        return topologyLinkRepository.findOneWithEagerRelationships(id).map(topologyLinkMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete TopologyLink : {}", id);
        topologyLinkRepository.deleteById(id);
    }
}
