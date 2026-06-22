package com.vht.ems.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.vht.ems.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlertEventDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlertEventDTO.class);
        AlertEventDTO alertEventDTO1 = new AlertEventDTO();
        alertEventDTO1.setId("id1");
        AlertEventDTO alertEventDTO2 = new AlertEventDTO();
        assertThat(alertEventDTO1).isNotEqualTo(alertEventDTO2);
        alertEventDTO2.setId(alertEventDTO1.getId());
        assertThat(alertEventDTO1).isEqualTo(alertEventDTO2);
        alertEventDTO2.setId("id2");
        assertThat(alertEventDTO1).isNotEqualTo(alertEventDTO2);
        alertEventDTO1.setId(null);
        assertThat(alertEventDTO1).isNotEqualTo(alertEventDTO2);
    }
}
