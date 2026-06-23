package com.vht.ems.web.rest;

import com.vht.ems.domain.enumeration.AlertStatus;
import com.vht.ems.domain.enumeration.DeviceStatus;
import com.vht.ems.domain.enumeration.Severity;
import com.vht.ems.repository.AlertEventRepository;
import com.vht.ems.repository.DeviceRepository;
import com.vht.ems.security.AuthoritiesConstants;
import com.vht.ems.service.dto.DashboardSummaryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Dashboard summary data.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardResource.class);

    private final DeviceRepository deviceRepository;
    private final AlertEventRepository alertEventRepository;

    public DashboardResource(DeviceRepository deviceRepository, AlertEventRepository alertEventRepository) {
        this.deviceRepository = deviceRepository;
        this.alertEventRepository = alertEventRepository;
    }

    /**
     * {@code GET /api/dashboard/summary} : aggregated counts for the dashboard.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        LOG.debug("REST request to get Dashboard summary");

        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setTotalDevices(deviceRepository.count());
        dto.setOnline(deviceRepository.countByStatus(DeviceStatus.ONLINE));
        dto.setOffline(deviceRepository.countByStatus(DeviceStatus.OFFLINE));
        dto.setUnknown(deviceRepository.countByStatus(DeviceStatus.UNKNOWN));

        long criticalAlerts = alertEventRepository.countByStatusAndSeverity(AlertStatus.OPEN, Severity.CRITICAL);
        long warningAlerts = alertEventRepository.countByStatusAndSeverity(AlertStatus.OPEN, Severity.WARNING);
        dto.setCriticalAlerts(criticalAlerts);
        dto.setWarningAlerts(warningAlerts);
        dto.setOpenAlerts(criticalAlerts + warningAlerts);

        return ResponseEntity.ok(dto);
    }
}
