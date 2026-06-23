package com.vht.ems.service;

import com.vht.ems.domain.Device;
import com.vht.ems.domain.MetricSample;

/**
 * Service for evaluating metric samples against alert rules
 * and managing the AlertEvent lifecycle.
 */
public interface AlertEvaluatorService {
    /**
     * Evaluate a metric sample against all enabled alert rules for the given device.
     * Creates, updates, or auto-resolves AlertEvents accordingly.
     * Broadcasts changes to /topic/alerts via WebSocket.
     *
     * @param device the device that generated the metric
     * @param sample the metric sample to evaluate
     */
    void evaluate(Device device, MetricSample sample);
}
