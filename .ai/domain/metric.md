# Domain: Metric

## Trách nhiệm
Lưu trữ các mẫu đo lường (CPU, RAM, Disk, latency) được thu thập định kỳ từ thiết bị. Đây là dữ liệu time-series — tăng nhanh theo thời gian, cần chiến lược dọn dẹp.

## MongoDB Document

```java
@Document(collection = "metric_samples")
public class MetricSample {
    @Id
    private String id;

    @Indexed                        // index để query theo device
    private String deviceId;

    @Indexed                        // index để query theo thời gian
    private Instant collectedAt;

    private Float cpuUsage;         // % (0–100)
    private Float ramUsage;         // % (0–100)
    private Float diskUsage;        // % (0–100)
    private Float pingLatencyMs;    // ms (TCP-connect time)
    private Boolean available;      // true nếu SSH thành công
}
```

## TTL Index (tự dọn data cũ)

```java
// Tự động xóa document sau 30 ngày
@CompoundIndex(def = "{'collectedAt': 1}", expireAfterSeconds = 2592000)
```
Hoặc cấu hình qua Liquibase/MongoTemplate khi khởi động.

## DTO

```java
public class MetricSampleDTO {
    private String id;
    private String deviceId;
    private Instant collectedAt;
    private Float cpuUsage;
    private Float ramUsage;
    private Float diskUsage;
    private Float pingLatencyMs;
    private Boolean available;
}
```

## REST API

| Method | Path | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/devices/{id}/metrics` | USER | Lịch sử metric của device (pagination) |
| GET | `/api/devices/{id}/metrics/latest` | USER | Mẫu mới nhất |
| GET | `/api/devices/{id}/metrics?from=&to=` | USER | Metric trong khoảng thời gian |

## Query Patterns

```java
// Mẫu mới nhất của 1 device
Optional<MetricSample> findTopByDeviceIdOrderByCollectedAtDesc(String deviceId);

// Lịch sử trong khoảng thời gian (cho biểu đồ)
List<MetricSample> findByDeviceIdAndCollectedAtBetween(
    String deviceId, Instant from, Instant to, Pageable pageable
);

// Metric trung bình 1 giờ gần nhất (dùng aggregation)
// db.metric_samples.aggregate([
//   { $match: { deviceId: "xxx", collectedAt: { $gte: <1h ago> } } },
//   { $group: { _id: null, avgCpu: { $avg: "$cpuUsage" }, avgRam: { $avg: "$ramUsage" } } }
// ])
```

## SSH Parsers (pure functions – dễ unit test)

```java
public class MetricParser {

    // Input: output của `free -m`
    // "Mem:           4096       3072       1024 ..."
    public static float parseRamUsagePercent(String freeOutput) { ... }

    // Input: diff của 2 lần đọc /proc/stat cách nhau 1 giây
    public static float parseCpuUsagePercent(String stat1, String stat2) { ... }

    // Input: output của `df -h /`
    // "/dev/sda1       20G   15G  5G  75% /"
    public static float parseDiskUsagePercent(String dfOutput) { ... }
}
```

Viết unit test với string cố định cho từng parser.

## Data Retention Strategy
- **TTL Index MongoDB**: tự xóa `MetricSample` sau 30 ngày
- **Downsampling** (nếu cần scale): job chạy hàng đêm, gộp sample 1 phút → 1 giờ cho data > 7 ngày
- **Hướng mở rộng**: migrate sang TimescaleDB (PostgreSQL) hoặc InfluxDB nếu số device > 500

## Liên kết
- Được tạo bởi `DeviceCollectorService` (xem `backend/collector.md`)
- `deviceId` tham chiếu đến `devices` collection
- Giá trị metric được Alert Engine đọc sau khi lưu (xem `domain/alert.md`)
