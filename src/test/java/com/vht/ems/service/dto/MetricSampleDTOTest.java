package com.vht.ems.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.vht.ems.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MetricSampleDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MetricSampleDTO.class);
        MetricSampleDTO metricSampleDTO1 = new MetricSampleDTO();
        metricSampleDTO1.setId("id1");
        MetricSampleDTO metricSampleDTO2 = new MetricSampleDTO();
        assertThat(metricSampleDTO1).isNotEqualTo(metricSampleDTO2);
        metricSampleDTO2.setId(metricSampleDTO1.getId());
        assertThat(metricSampleDTO1).isEqualTo(metricSampleDTO2);
        metricSampleDTO2.setId("id2");
        assertThat(metricSampleDTO1).isNotEqualTo(metricSampleDTO2);
        metricSampleDTO1.setId(null);
        assertThat(metricSampleDTO1).isNotEqualTo(metricSampleDTO2);
    }
}
