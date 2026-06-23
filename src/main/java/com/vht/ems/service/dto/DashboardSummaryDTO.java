package com.vht.ems.service.dto;

import java.io.Serializable;

/**
 * DTO for the Dashboard summary endpoint.
 * Aggregates device counts (by status) and open alert counts (by severity).
 */
public class DashboardSummaryDTO implements Serializable {

    private long totalDevices;
    private long online;
    private long offline;
    private long unknown;
    private long openAlerts;
    private long criticalAlerts;
    private long warningAlerts;

    public long getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(long totalDevices) {
        this.totalDevices = totalDevices;
    }

    public long getOnline() {
        return online;
    }

    public void setOnline(long online) {
        this.online = online;
    }

    public long getOffline() {
        return offline;
    }

    public void setOffline(long offline) {
        this.offline = offline;
    }

    public long getUnknown() {
        return unknown;
    }

    public void setUnknown(long unknown) {
        this.unknown = unknown;
    }

    public long getOpenAlerts() {
        return openAlerts;
    }

    public void setOpenAlerts(long openAlerts) {
        this.openAlerts = openAlerts;
    }

    public long getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(long criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public long getWarningAlerts() {
        return warningAlerts;
    }

    public void setWarningAlerts(long warningAlerts) {
        this.warningAlerts = warningAlerts;
    }
}
