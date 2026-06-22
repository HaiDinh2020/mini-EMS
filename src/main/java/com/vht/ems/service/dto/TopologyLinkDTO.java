package com.vht.ems.service.dto;

import com.vht.ems.domain.enumeration.LinkStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.vht.ems.domain.TopologyLink} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TopologyLinkDTO implements Serializable {

    private String id;

    @Size(max = 255)
    private String linkType;

    private Double bandwidthMbps;

    @NotNull
    private LinkStatus status;

    private DeviceDTO sourceDevice;

    private DeviceDTO targetDevice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public Double getBandwidthMbps() {
        return bandwidthMbps;
    }

    public void setBandwidthMbps(Double bandwidthMbps) {
        this.bandwidthMbps = bandwidthMbps;
    }

    public LinkStatus getStatus() {
        return status;
    }

    public void setStatus(LinkStatus status) {
        this.status = status;
    }

    public DeviceDTO getSourceDevice() {
        return sourceDevice;
    }

    public void setSourceDevice(DeviceDTO sourceDevice) {
        this.sourceDevice = sourceDevice;
    }

    public DeviceDTO getTargetDevice() {
        return targetDevice;
    }

    public void setTargetDevice(DeviceDTO targetDevice) {
        this.targetDevice = targetDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TopologyLinkDTO)) {
            return false;
        }

        TopologyLinkDTO topologyLinkDTO = (TopologyLinkDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, topologyLinkDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TopologyLinkDTO{" +
            "id='" + getId() + "'" +
            ", linkType='" + getLinkType() + "'" +
            ", bandwidthMbps=" + getBandwidthMbps() +
            ", status='" + getStatus() + "'" +
            ", sourceDevice=" + getSourceDevice() +
            ", targetDevice=" + getTargetDevice() +
            "}";
    }
}
