package com.vht.ems.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vht.ems.domain.enumeration.LinkStatus;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A TopologyLink.
 */
@Document(collection = "topology_link")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TopologyLink implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Size(max = 255)
    @Field("link_type")
    private String linkType;

    @Field("bandwidth_mbps")
    private Double bandwidthMbps;

    @NotNull
    @Field("status")
    private LinkStatus status;

    @DBRef
    @Field("sourceDevice")
    @JsonIgnoreProperties(value = { "credential" }, allowSetters = true)
    private Device sourceDevice;

    @DBRef
    @Field("targetDevice")
    @JsonIgnoreProperties(value = { "credential" }, allowSetters = true)
    private Device targetDevice;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public TopologyLink id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkType() {
        return this.linkType;
    }

    public TopologyLink linkType(String linkType) {
        this.setLinkType(linkType);
        return this;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public Double getBandwidthMbps() {
        return this.bandwidthMbps;
    }

    public TopologyLink bandwidthMbps(Double bandwidthMbps) {
        this.setBandwidthMbps(bandwidthMbps);
        return this;
    }

    public void setBandwidthMbps(Double bandwidthMbps) {
        this.bandwidthMbps = bandwidthMbps;
    }

    public LinkStatus getStatus() {
        return this.status;
    }

    public TopologyLink status(LinkStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(LinkStatus status) {
        this.status = status;
    }

    public Device getSourceDevice() {
        return this.sourceDevice;
    }

    public void setSourceDevice(Device device) {
        this.sourceDevice = device;
    }

    public TopologyLink sourceDevice(Device device) {
        this.setSourceDevice(device);
        return this;
    }

    public Device getTargetDevice() {
        return this.targetDevice;
    }

    public void setTargetDevice(Device device) {
        this.targetDevice = device;
    }

    public TopologyLink targetDevice(Device device) {
        this.setTargetDevice(device);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TopologyLink)) {
            return false;
        }
        return getId() != null && getId().equals(((TopologyLink) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TopologyLink{" +
            "id=" + getId() +
            ", linkType='" + getLinkType() + "'" +
            ", bandwidthMbps=" + getBandwidthMbps() +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
