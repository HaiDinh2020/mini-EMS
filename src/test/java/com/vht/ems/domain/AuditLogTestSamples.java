package com.vht.ems.domain;

import java.util.UUID;

public class AuditLogTestSamples {

    public static AuditLog getAuditLogSample1() {
        return new AuditLog().id("id1").username("username1").action("action1").entityName("entityName1").entityId("entityId1");
    }

    public static AuditLog getAuditLogSample2() {
        return new AuditLog().id("id2").username("username2").action("action2").entityName("entityName2").entityId("entityId2");
    }

    public static AuditLog getAuditLogRandomSampleGenerator() {
        return new AuditLog()
            .id(UUID.randomUUID().toString())
            .username(UUID.randomUUID().toString())
            .action(UUID.randomUUID().toString())
            .entityName(UUID.randomUUID().toString())
            .entityId(UUID.randomUUID().toString());
    }
}
