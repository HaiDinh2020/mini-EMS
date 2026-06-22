package com.vht.ems.service.impl;

import com.vht.ems.domain.Device;
import com.vht.ems.domain.enumeration.DeviceStatus;
import com.vht.ems.repository.DeviceRepository;
import com.vht.ems.service.DeviceService;
import com.vht.ems.service.dto.DeviceDTO;
import com.vht.ems.service.mapper.DeviceMapper;
import com.vht.ems.web.rest.errors.BadRequestAlertException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.vht.ems.domain.Device}.
 */
@Service
public class DeviceServiceImpl implements DeviceService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceServiceImpl.class);

    private final DeviceRepository deviceRepository;

    private final DeviceMapper deviceMapper;

    public DeviceServiceImpl(DeviceRepository deviceRepository, DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    @Override
    public DeviceDTO save(DeviceDTO deviceDTO) {
        LOG.debug("Request to save Device : {}", deviceDTO);
        
        if (deviceRepository.existsByIpAddress(deviceDTO.getIpAddress())) {
            throw new BadRequestAlertException("Device with this IP address already exists", "device", "ipaddressexists");
        }

        if (deviceDTO.getStatus() == null) {
            deviceDTO.setStatus(DeviceStatus.UNKNOWN);
        }
        if (deviceDTO.getMonitoringEnabled() == null) {
            deviceDTO.setMonitoringEnabled(true);
        }
        if (deviceDTO.getSshPort() == null) {
            deviceDTO.setSshPort(22);
        }

        Device device = deviceMapper.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        return deviceMapper.toDto(device);
    }

    @Override
    public DeviceDTO update(DeviceDTO deviceDTO) {
        LOG.debug("Request to update Device : {}", deviceDTO);
        Device device = deviceMapper.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        return deviceMapper.toDto(device);
    }

    @Override
    public Optional<DeviceDTO> partialUpdate(DeviceDTO deviceDTO) {
        LOG.debug("Request to partially update Device : {}", deviceDTO);

        return deviceRepository
            .findById(deviceDTO.getId())
            .map(existingDevice -> {
                deviceMapper.partialUpdate(existingDevice, deviceDTO);

                return existingDevice;
            })
            .map(deviceRepository::save)
            .map(deviceMapper::toDto);
    }

    @Override
    public Page<DeviceDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Devices");
        return deviceRepository.findAll(pageable).map(deviceMapper::toDto);
    }

    public Page<DeviceDTO> findAllWithEagerRelationships(Pageable pageable) {
        return deviceRepository.findAllWithEagerRelationships(pageable).map(deviceMapper::toDto);
    }

    @Override
    public Optional<DeviceDTO> findOne(String id) {
        LOG.debug("Request to get Device : {}", id);
        return deviceRepository.findOneWithEagerRelationships(id).map(deviceMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Device : {}", id);
        deviceRepository.deleteById(id);
    }
}
