package com.vht.ems.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.vht.ems.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlertRuleDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlertRuleDTO.class);
        AlertRuleDTO alertRuleDTO1 = new AlertRuleDTO();
        alertRuleDTO1.setId("id1");
        AlertRuleDTO alertRuleDTO2 = new AlertRuleDTO();
        assertThat(alertRuleDTO1).isNotEqualTo(alertRuleDTO2);
        alertRuleDTO2.setId(alertRuleDTO1.getId());
        assertThat(alertRuleDTO1).isEqualTo(alertRuleDTO2);
        alertRuleDTO2.setId("id2");
        assertThat(alertRuleDTO1).isNotEqualTo(alertRuleDTO2);
        alertRuleDTO1.setId(null);
        assertThat(alertRuleDTO1).isNotEqualTo(alertRuleDTO2);
    }
}
