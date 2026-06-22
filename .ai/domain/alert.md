# Domain: Alert

## Trách nhiệm
Định nghĩa ngưỡng cảnh báo (AlertRule) và quản lý vòng đời sự kiện cảnh báo (AlertEvent): OPEN → ACKNOWLEDGED → RESOLVED.

## MongoDB Documents

### AlertRule
```java
@Document(collection = "alert_rules")
public class AlertRule {
    @Id
    private String id;

    private String deviceId;        // null = áp dụng cho tất cả device
    private MetricType metricType;  // CPU, RAM, DISK, PING_LATENCY
    private Float thresholdWarning;
    private Float thresholdCritical;
    private Boolean enabled;
}
```

### AlertEvent
```java
@Document(collection = "alert_events")
public class AlertEvent {
    @Id
    private String id;

    @Indexed
    private String deviceId;

    private String ruleId;
    private MetricType metricType;
    private Float value;            // giá trị tại thời điểm trigger
    private Severity severity;      // WARNING hoặc CRITICAL
    private String message;
    private Instant triggeredAt;
    private Instant resolvedAt;

    @Indexed
    private AlertStatus status;     // OPEN, ACKNOWLEDGED, RESOLVED
}
```

## Enums

```java
enum MetricType { CPU, RAM, DISK, PING_LATENCY, AVAILABILITY }
enum Severity   { WARNING, CRITICAL }
enum AlertStatus { OPEN, ACKNOWLEDGED, RESOLVED }
```

## Ngưỡng mặc định

| Metric | WARNING | CRITICAL |
|---|---|---|
| CPU | ≥ 85% | ≥ 95% |
| RAM | ≥ 80% | ≥ 90% |
| Disk | ≥ 75% | ≥ 90% |
| Ping Latency | ≥ 200ms | ≥ 500ms |

## Alert Engine Logic

```
evaluate(device, metricSample):
  for each enabled AlertRule matching device:
    value = metricSample.getValue(rule.metricType)

    if value >= rule.thresholdCritical:
      createOrUpdate(device, rule, CRITICAL, value)

    else if value >= rule.thresholdWarning:
      createOrUpdate(device, rule, WARNING, value)

    else:
      autoResolve(device, rule)   // nếu đang có OPEN alert → RESOLVED

createOrUpdate:
  existingOpen = findOpenAlert(deviceId, ruleId)
  if exists and severity same → chỉ update value/timestamp
  if exists and severity changed → update severity, broadcast
  if not exists → tạo mới AlertEvent, broadcast to /topic/alerts

autoResolve:
  openAlert = findOpenAlert(deviceId, ruleId)
  if exists:
    openAlert.status = RESOLVED
    openAlert.resolvedAt = now()
    save + broadcast to /topic/alerts
```

## REST API

| Method | Path | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/alert-rules` | USER | Danh sách rule |
| POST | `/api/alert-rules` | ADMIN | Tạo rule mới |
| PUT | `/api/alert-rules/{id}` | ADMIN | Cập nhật rule |
| DELETE | `/api/alert-rules/{id}` | ADMIN | Xóa rule |
| GET | `/api/alert-events` | USER | Danh sách event (filter status/severity) |
| GET | `/api/alert-events/{id}` | USER | Chi tiết event |
| PUT | `/api/alert-events/{id}/acknowledge` | USER | Chuyển sang ACKNOWLEDGED |

## Query Patterns

```java
// Alert đang mở theo device (cho dashboard badge)
List<AlertEvent> findByDeviceIdAndStatus(String deviceId, AlertStatus status);

// Đếm alert theo severity (cho dashboard summary)
long countByStatusAndSeverity(AlertStatus status, Severity severity);

// Alert rule apply cho device (device-specific hoặc global)
List<AlertRule> findByEnabledTrueAndDeviceIdInOrDeviceIdIsNull(List<String> deviceIds);
```

## WebSocket Payload (khi broadcast)

```json
{
  "type": "ALERT_EVENT",
  "alertEventId": "abc123",
  "deviceId": "dev456",
  "deviceName": "node-amf",
  "metricType": "RAM",
  "severity": "CRITICAL",
  "value": 91.5,
  "message": "RAM usage 91.5% exceeds critical threshold 90%",
  "status": "OPEN",
  "timestamp": "2025-06-10T10:30:00Z"
}
```

## Liên kết
- AlertEngine được gọi từ `DeviceCollectorService` sau mỗi MetricSample (xem `backend/collector.md`)
- Broadcast qua WebSocket (xem `backend/websocket.md`)
- Mọi thay đổi AlertRule được ghi vào AuditLog (xem `domain/audit.md`)
