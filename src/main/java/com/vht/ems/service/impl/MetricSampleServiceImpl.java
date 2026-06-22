package com.vht.ems.service.impl;

import com.vht.ems.domain.MetricSample;
import com.vht.ems.repository.MetricSampleRepository;
import com.vht.ems.service.MetricSampleService;
import com.vht.ems.service.dto.MetricSampleDTO;
import com.vht.ems.service.mapper.MetricSampleMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.vht.ems.domain.MetricSample}.
 */
@Service
public class MetricSampleServiceImpl implements MetricSampleService {

    private static final Logger LOG = LoggerFactory.getLogger(MetricSampleServiceImpl.class);

    private final MetricSampleRepository metricSampleRepository;

    private final MetricSampleMapper metricSampleMapper;

    public MetricSampleServiceImpl(MetricSampleRepository metricSampleRepository, MetricSampleMapper metricSampleMapper) {
        this.metricSampleRepository = metricSampleRepository;
        this.metricSampleMapper = metricSampleMapper;
    }

    @Override
    public MetricSampleDTO save(MetricSampleDTO metricSampleDTO) {
        LOG.debug("Request to save MetricSample : {}", metricSampleDTO);
        MetricSample metricSample = metricSampleMapper.toEntity(metricSampleDTO);
        metricSample = metricSampleRepository.save(metricSample);
        return metricSampleMapper.toDto(metricSample);
    }

    @Override
    public MetricSampleDTO update(MetricSampleDTO metricSampleDTO) {
        LOG.debug("Request to update MetricSample : {}", metricSampleDTO);
        MetricSample metricSample = metricSampleMapper.toEntity(metricSampleDTO);
        metricSample = metricSampleRepository.save(metricSample);
        return metricSampleMapper.toDto(metricSample);
    }

    @Override
    public Optional<MetricSampleDTO> partialUpdate(MetricSampleDTO metricSampleDTO) {
        LOG.debug("Request to partially update MetricSample : {}", metricSampleDTO);

        return metricSampleRepository
            .findById(metricSampleDTO.getId())
            .map(existingMetricSample -> {
                metricSampleMapper.partialUpdate(existingMetricSample, metricSampleDTO);

                return existingMetricSample;
            })
            .map(metricSampleRepository::save)
            .map(metricSampleMapper::toDto);
    }

    @Override
    public Page<MetricSampleDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all MetricSamples");
        return metricSampleRepository.findAll(pageable).map(metricSampleMapper::toDto);
    }

    public Page<MetricSampleDTO> findAllWithEagerRelationships(Pageable pageable) {
        return metricSampleRepository.findAllWithEagerRelationships(pageable).map(metricSampleMapper::toDto);
    }

    @Override
    public Optional<MetricSampleDTO> findOne(String id) {
        LOG.debug("Request to get MetricSample : {}", id);
        return metricSampleRepository.findOneWithEagerRelationships(id).map(metricSampleMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete MetricSample : {}", id);
        metricSampleRepository.deleteById(id);
    }
}
