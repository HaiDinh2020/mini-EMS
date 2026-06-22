package com.vht.ems.domain;

import java.util.UUID;

public class CredentialTestSamples {

    public static Credential getCredentialSample1() {
        return new Credential().id("id1").name("name1").username("username1").encryptedSecret("encryptedSecret1");
    }

    public static Credential getCredentialSample2() {
        return new Credential().id("id2").name("name2").username("username2").encryptedSecret("encryptedSecret2");
    }

    public static Credential getCredentialRandomSampleGenerator() {
        return new Credential()
            .id(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .username(UUID.randomUUID().toString())
            .encryptedSecret(UUID.randomUUID().toString());
    }
}
