package com.vht.ems.web.rest;

import com.vht.ems.domain.Device;
import com.vht.ems.domain.TopologyLink;
import com.vht.ems.repository.DeviceRepository;
import com.vht.ems.repository.TopologyLinkRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller providing lightweight topology data for the vis-network frontend graph.
 * Separate from TopologyLinkResource (JHipster CRUD) to return a trimmed shape.
 */
@RestController
@RequestMapping("/api/topology")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
public class TopologyResource {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyResource.class);

    public record TopologyNode(String id, String name, String deviceType, String status, String ipAddress) {}

    public record TopologyEdge(
        String id,
        String linkType,
        Double bandwidthMbps,
        String status,
        String sourceId,
        String sourceName,
        String targetId,
        String targetName
    ) {}

    private final DeviceRepository deviceRepository;
    private final TopologyLinkRepository topologyLinkRepository;

    public TopologyResource(DeviceRepository deviceRepository, TopologyLinkRepository topologyLinkRepository) {
        this.deviceRepository = deviceRepository;
        this.topologyLinkRepository = topologyLinkRepository;
    }

    /**
     * GET /api/topology/devices – trimmed device list for graph nodes.
     */
    @GetMapping("/devices")
    public List<TopologyNode> getTopologyDevices() {
        LOG.debug("REST request to get topology devices");
        return deviceRepository.findAll().stream().map(this::toNode).toList();
    }

    /**
     * GET /api/topology/links – all topology links with source/target device info.
     */
    @GetMapping("/links")
    public List<TopologyEdge> getTopologyLinks() {
        LOG.debug("REST request to get topology links");
        return topologyLinkRepository.findAllWithEagerRelationships().stream().map(this::toEdge).toList();
    }

    private TopologyNode toNode(Device d) {
        return new TopologyNode(
            d.getId(),
            d.getName(),
            d.getDeviceType() != null ? d.getDeviceType().name() : null,
            d.getStatus() != null ? d.getStatus().name() : "UNKNOWN",
            d.getIpAddress()
        );
    }

    private TopologyEdge toEdge(TopologyLink l) {
        String sourceId = l.getSourceDevice() != null ? l.getSourceDevice().getId() : null;
        String sourceName = l.getSourceDevice() != null ? l.getSourceDevice().getName() : null;
        String targetId = l.getTargetDevice() != null ? l.getTargetDevice().getId() : null;
        String targetName = l.getTargetDevice() != null ? l.getTargetDevice().getName() : null;
        return new TopologyEdge(
            l.getId(),
            l.getLinkType(),
            l.getBandwidthMbps(),
            l.getStatus() != null ? l.getStatus().name() : null,
            sourceId,
            sourceName,
            targetId,
            targetName
        );
    }
}
