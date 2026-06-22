package com.vht.ems.domain;

import static com.vht.ems.domain.CredentialTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.vht.ems.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CredentialTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Credential.class);
        Credential credential1 = getCredentialSample1();
        Credential credential2 = new Credential();
        assertThat(credential1).isNotEqualTo(credential2);

        credential2.setId(credential1.getId());
        assertThat(credential1).isEqualTo(credential2);

        credential2 = getCredentialSample2();
        assertThat(credential1).isNotEqualTo(credential2);
    }
}
