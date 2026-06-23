package com.vht.ems.service;

import com.vht.ems.domain.Device;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes device status updates to WebSocket topic /topic/device-status.
 * Called by DeviceCollectorService (Sprint 2) after each connectivity check.
 */
@Service
public class DeviceStatusPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceStatusPublisher.class);
    private static final String DEVICE_STATUS_TOPIC = "/topic/device-status";

    private final SimpMessagingTemplate messagingTemplate;

    public DeviceStatusPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishDeviceStatus(Device device) {
        try {
            Map<String, Object> payload = Map.of(
                "deviceId",
                device.getId(),
                "name",
                device.getName() != null ? device.getName() : "",
                "status",
                device.getStatus() != null ? device.getStatus().name() : "UNKNOWN",
                "lastCheckedAt",
                device.getLastCheckedAt() != null ? device.getLastCheckedAt().toString() : ""
            );
            messagingTemplate.convertAndSend(DEVICE_STATUS_TOPIC, (Object) payload);
        } catch (Exception e) {
            LOG.warn("Failed to publish device status for device {}: {}", device.getId(), e.getMessage());
        }
    }
}
