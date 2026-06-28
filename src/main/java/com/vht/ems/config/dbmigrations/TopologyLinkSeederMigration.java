package com.vht.ems.config.dbmigrations;

import com.vht.ems.domain.Device;
import com.vht.ems.domain.TopologyLink;
import com.vht.ems.domain.enumeration.LinkStatus;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.util.List;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * Seeds topology links between the 5 simulated 5G Core devices:
 *   gNodeB --[N2]--> AMF --[N11]--> SMF --[N4]--> UPF
 *                    AMF --[N8]---> UDM
 */
@ChangeUnit(id = "5g-topology-links-initialization", order = "004")
public class TopologyLinkSeederMigration {

    private final MongoTemplate template;

    public TopologyLinkSeederMigration(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        Device gnodeb = findById("device-5g-gnodeb");
        Device amf = findById("device-5g-amf");
        Device smf = findById("device-5g-smf");
        Device upf = findById("device-5g-upf");
        Device udm = findById("device-5g-udm");

        if (gnodeb == null || amf == null || smf == null || upf == null || udm == null) {
            return;
        }

        List<TopologyLink> links = List.of(
            buildLink("link-5g-n2", gnodeb, amf, "N2", 10000.0),
            buildLink("link-5g-n11", amf, smf, "N11", 10000.0),
            buildLink("link-5g-n4", smf, upf, "N4", 25000.0),
            buildLink("link-5g-n8", amf, udm, "N8", 10000.0)
        );

        for (TopologyLink link : links) {
            boolean exists = template.exists(Query.query(Criteria.where("_id").is(link.getId())), TopologyLink.class);
            if (!exists) {
                template.save(link);
            }
        }
    }

    @RollbackExecution
    public void rollback() {
        List<String> linkIds = List.of("link-5g-n2", "link-5g-n11", "link-5g-n4", "link-5g-n8");
        for (String id : linkIds) {
            template.remove(Query.query(Criteria.where("_id").is(id)), TopologyLink.class);
        }
    }

    private Device findById(String id) {
        return template.findById(id, Device.class);
    }

    private TopologyLink buildLink(String id, Device src, Device tgt, String type, Double bwMbps) {
        TopologyLink link = new TopologyLink();
        link.setId(id);
        link.setSourceDevice(src);
        link.setTargetDevice(tgt);
        link.setLinkType(type);
        link.setBandwidthMbps(bwMbps);
        link.setStatus(LinkStatus.UP);
        return link;
    }
}
