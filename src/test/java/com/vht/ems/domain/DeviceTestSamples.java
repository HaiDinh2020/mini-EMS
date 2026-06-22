package com.vht.ems.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Device getDeviceSample1() {
        return new Device()
            .id("id1")
            .name("name1")
            .ipAddress("ipAddress1")
            .hostname("hostname1")
            .vendor("vendor1")
            .model("model1")
            .sshPort(1)
            .sshUsername("sshUsername1")
            .location("location1");
    }

    public static Device getDeviceSample2() {
        return new Device()
            .id("id2")
            .name("name2")
            .ipAddress("ipAddress2")
            .hostname("hostname2")
            .vendor("vendor2")
            .model("model2")
            .sshPort(2)
            .sshUsername("sshUsername2")
            .location("location2");
    }

    public static Device getDeviceRandomSampleGenerator() {
        return new Device()
            .id(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .ipAddress(UUID.randomUUID().toString())
            .hostname(UUID.randomUUID().toString())
            .vendor(UUID.randomUUID().toString())
            .model(UUID.randomUUID().toString())
            .sshPort(intCount.incrementAndGet())
            .sshUsername(UUID.randomUUID().toString())
            .location(UUID.randomUUID().toString());
    }
}
