package com.vht.ems.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.vht.ems.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TopologyLinkDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TopologyLinkDTO.class);
        TopologyLinkDTO topologyLinkDTO1 = new TopologyLinkDTO();
        topologyLinkDTO1.setId("id1");
        TopologyLinkDTO topologyLinkDTO2 = new TopologyLinkDTO();
        assertThat(topologyLinkDTO1).isNotEqualTo(topologyLinkDTO2);
        topologyLinkDTO2.setId(topologyLinkDTO1.getId());
        assertThat(topologyLinkDTO1).isEqualTo(topologyLinkDTO2);
        topologyLinkDTO2.setId("id2");
        assertThat(topologyLinkDTO1).isNotEqualTo(topologyLinkDTO2);
        topologyLinkDTO1.setId(null);
        assertThat(topologyLinkDTO1).isNotEqualTo(topologyLinkDTO2);
    }
}
