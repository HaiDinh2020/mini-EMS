package com.vht.ems.service.collector;

/**
 * Pure-function SSH output parsers — no I/O, fully unit-testable with fixed string inputs.
 */
public final class MetricParser {

    private MetricParser() {}

    /**
     * Parse CPU usage (%) from two consecutive /proc/stat readings taken ~1 s apart.
     *
     * Format of each line: "cpu  user nice system idle iowait irq softirq steal guest guest_nice"
     * Returns 0.0 if parsing fails (non-crashing degraded mode).
     */
    public static double parseCpuUsagePercent(String stat1, String stat2) {
        try {
            long[] t1 = parseProcStatLine(stat1);
            long[] t2 = parseProcStatLine(stat2);

            long idle1 = t1[3] + t1[4]; // idle + iowait
            long idle2 = t2[3] + t2[4];
            long total1 = sum(t1);
            long total2 = sum(t2);

            long deltaTotal = total2 - total1;
            long deltaIdle = idle2 - idle1;

            if (deltaTotal <= 0) return 0.0;
            double usage = (100.0 * (deltaTotal - deltaIdle)) / (double) deltaTotal;
            return Math.max(0.0, Math.min(100.0, usage));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Parse RAM usage (%) from the output of `free -m`.
     *
     * Expected format (second line):
     *   Mem:   totalMb  usedMb  freeMb  sharedMb  buffCacheMb  availableMb
     */
    public static double parseRamUsagePercent(String freeOutput) {
        try {
            for (String line : freeOutput.split("\\r?\\n")) {
                if (line.startsWith("Mem:")) {
                    String[] parts = line.trim().split("\\s+");
                    double total = Double.parseDouble(parts[1]);
                    double used = Double.parseDouble(parts[2]);
                    if (total <= 0) return 0.0;
                    return Math.max(0.0, Math.min(100.0, (used / total) * 100.0));
                }
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    /**
     * Parse disk usage (%) from the output of `df -h /`.
     *
     * Expected last line format:
     *   /dev/sda1  20G  15G  5G  75%  /
     * Returns the integer percentage in the "Use%" column (5th token).
     */
    public static double parseDiskUsagePercent(String dfOutput) {
        try {
            String[] lines = dfOutput.trim().split("\\r?\\n");
            // last non-empty line is the data row
            for (int i = lines.length - 1; i >= 0; i--) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.startsWith("Filesystem")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String pct = parts[4].replace("%", "");
                    return Double.parseDouble(pct);
                }
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    // --- helpers ---

    private static long[] parseProcStatLine(String statOutput) {
        for (String line : statOutput.split("\\r?\\n")) {
            if (line.startsWith("cpu ") || line.startsWith("cpu\t")) {
                String[] parts = line.trim().split("\\s+");
                // indices: 0=cpu label, 1=user, 2=nice, 3=system, 4=idle, 5=iowait, ...
                long[] values = new long[parts.length - 1];
                for (int i = 0; i < values.length; i++) {
                    values[i] = Long.parseLong(parts[i + 1]);
                }
                return values;
            }
        }
        throw new IllegalArgumentException("No 'cpu' line found in /proc/stat output");
    }

    private static long sum(long[] arr) {
        long s = 0;
        for (long v : arr) s += v;
        return s;
    }
}
