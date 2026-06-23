package com.vht.ems.service.collector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class MetricParserTest {

    // ---- CPU ----

    @Test
    void parseCpu_normalLoad_returnsExpectedPercent() {
        // ~25 % CPU usage: delta busy = 250, delta total = 1000
        String stat1 = "cpu  1000 0 500 3000 200 0 0 0 0 0\n";
        String stat2 = "cpu  1200 0 550 3700 250 0 0 0 0 0\n";

        double result = MetricParser.parseCpuUsagePercent(stat1, stat2);

        // deltaTotal = (1200+0+550+3700+250) - (1000+0+500+3000+200) = 5700 - 4700 = 1000
        // deltaIdle  = (3700+250) - (3000+200) = 3950 - 3200 = 750
        // usage      = 100 * (1000-750) / 1000 = 25%
        assertThat(result).isCloseTo(25.0, within(0.1));
    }

    @Test
    void parseCpu_idleOnly_returnsNearZero() {
        String stat1 = "cpu  100 0 100 9000 100 0 0 0 0 0\n";
        String stat2 = "cpu  100 0 100 10000 100 0 0 0 0 0\n";

        double result = MetricParser.parseCpuUsagePercent(stat1, stat2);

        assertThat(result).isCloseTo(0.0, within(1.0));
    }

    @Test
    void parseCpu_invalidInput_returnsZero() {
        assertThat(MetricParser.parseCpuUsagePercent("bad data", "bad data")).isEqualTo(0.0);
    }

    // ---- RAM ----

    @Test
    void parseRam_typical_returnsCorrectPercent() {
        // total=4096, used=3072 → 75%
        String freeOutput =
            "              total        used        free      shared  buff/cache   available\n" +
            "Mem:           4096        3072        1024           0           0         512\n" +
            "Swap:          2048           0        2048\n";

        double result = MetricParser.parseRamUsagePercent(freeOutput);

        assertThat(result).isCloseTo(75.0, within(0.1));
    }

    @Test
    void parseRam_lowUsage_returnsCorrectPercent() {
        // total=8192, used=512 → ~6.25%
        String freeOutput =
            "              total        used        free      shared  buff/cache   available\n" +
            "Mem:           8192         512        7680           0           0        7000\n";

        double result = MetricParser.parseRamUsagePercent(freeOutput);

        assertThat(result).isCloseTo(6.25, within(0.1));
    }

    @Test
    void parseRam_invalidInput_returnsZero() {
        assertThat(MetricParser.parseRamUsagePercent("not valid output")).isEqualTo(0.0);
    }

    // ---- Disk ----

    @Test
    void parseDisk_typical_returnsCorrectPercent() {
        String dfOutput = "Filesystem      Size  Used Avail Use% Mounted on\n" + "/dev/sda1        20G   15G  5.0G  75% /\n";

        double result = MetricParser.parseDiskUsagePercent(dfOutput);

        assertThat(result).isCloseTo(75.0, within(0.1));
    }

    @Test
    void parseDisk_almostFull_returnsHighPercent() {
        String dfOutput = "Filesystem      Size  Used Avail Use% Mounted on\n" + "overlay         100G   91G  9.0G  91% /\n";

        double result = MetricParser.parseDiskUsagePercent(dfOutput);

        assertThat(result).isCloseTo(91.0, within(0.1));
    }

    @Test
    void parseDisk_invalidInput_returnsZero() {
        assertThat(MetricParser.parseDiskUsagePercent("bad")).isEqualTo(0.0);
    }

    // ---- boundary ----

    @Test
    void parseCpu_resultClampedTo100() {
        // Pathological input where calc would exceed 100
        String stat1 = "cpu  0 0 0 100 0 0 0 0 0 0\n";
        String stat2 = "cpu  1000 0 0 0 0 0 0 0 0 0\n";

        double result = MetricParser.parseCpuUsagePercent(stat1, stat2);

        assertThat(result).isLessThanOrEqualTo(100.0);
        assertThat(result).isGreaterThanOrEqualTo(0.0);
    }
}
