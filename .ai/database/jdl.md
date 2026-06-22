# Database Domain: Data Model (MongoDB)

## Lưu ý quan trọng
Kế hoạch gốc dùng JHipster JDL + PostgreSQL (quan hệ, Liquibase). Dự án này dùng **MongoDB** — không có schema cứng, không có Liquibase, không có JOIN thật. Các quan hệ `ManyToOne` trong JDL gốc được chuyển thành **tham chiếu bằng ObjectId** (reference pattern) hoặc **embed** tuỳ mức độ truy vấn cùng nhau.

## Quyết định kiến trúc dữ liệu
| Vấn đề | JDL gốc (SQL) | MongoDB |
|---|---|---|
| Quan hệ Device–Credential | FK `Device.credential_id` | Lưu `credentialId: ObjectId` (ref) trong document `Device` |
| Quan hệ MetricSample–Device | FK `MetricSample.device_id` | Lưu `deviceId: ObjectId` (ref) trong document `MetricSample` |
| Quan hệ AlertEvent–Device/Rule | 2 FK | Lưu `deviceId`, `ruleId` (ref) trong `AlertEvent` |
| Quan hệ TopologyLink–Device (source/target) | 2 FK | Lưu `sourceDeviceId`, `targetDeviceId` (ref) |
| Schema validation | Liquibase changelog | JSON Schema validator của MongoDB (`$jsonSchema`) trên từng collection, hoặc validate ở tầng Spring Data (Bean Validation) |
| Migration | Liquibase versioned changelog | Dùng Mongock hoặc script migration tự viết, chạy lúc app khởi động |

## Collections

### `devices`
```json
{
  "_id": ObjectId,
  "name": "string (required)",
  "ipAddress": "string (required)",
  "hostname": "string",
  "deviceType": "SERVER|ROUTER|SWITCH|FIREWALL|GNODEB|AMF|SMF|UPF|UDM|OTHER",
  "vendor": "string",
  "model": "string",
  "sshPort": "number",
  "sshUsername": "string",
  "location": "string",
  "status": "ONLINE|OFFLINE|UNKNOWN|WARNING|CRITICAL",
  "lastCheckedAt": "Date (Instant)",
  "monitoringEnabled": "boolean",
  "description": "string",
  "credentialId": "ObjectId (ref credentials)"
}
```

### `credentials`
```json
{
  "_id": ObjectId,
  "name": "string",
  "authType": "PASSWORD|SSH_KEY",
  "username": "string",
  "encryptedSecret": "string (Jasypt, never plaintext)"
}
```

### `metric_samples` (time-series, ghi nhiều, không update)
```json
{
  "_id": ObjectId,
  "deviceId": "ObjectId (ref devices)",
  "cpuUsage": "number",
  "ramUsage": "number",
  "diskUsage": "number",
  "pingLatencyMs": "number",
  "collectedAt": "Date (Instant, required)"
}
```
Nên dùng **MongoDB Time Series Collection** (`db.createCollection("metric_samples", { timeseries: { timeField: "collectedAt", metaField: "deviceId", granularity: "seconds" } })`) để tối ưu lưu trữ/nén dữ liệu tăng nhanh theo thời gian.

### `alert_rules`
```json
{
  "_id": ObjectId,
  "metricType": "CPU|RAM|DISK|PING_LATENCY|AVAILABILITY",
  "thresholdWarning": "number",
  "thresholdCritical": "number",
  "enabled": "boolean"
}
```

### `alert_events`
```json
{
  "_id": ObjectId,
  "deviceId": "ObjectId (ref devices)",
  "ruleId": "ObjectId (ref alert_rules)",
  "metricType": "CPU|RAM|DISK|PING_LATENCY|AVAILABILITY",
  "value": "number",
  "severity": "WARNING|CRITICAL",
  "message": "string",
  "triggeredAt": "Date",
  "resolvedAt": "Date",
  "status": "OPEN|ACKNOWLEDGED|RESOLVED"
}
```

### `topology_links`
```json
{
  "_id": ObjectId,
  "sourceDeviceId": "ObjectId (ref devices)",
  "targetDeviceId": "ObjectId (ref devices)",
  "linkType": "string",
  "bandwidthMbps": "number",
  "status": "UP|DOWN|DEGRADED"
}
```

### `audit_logs`
```json
{
  "_id": ObjectId,
  "username": "string",
  "action": "string",
  "entityName": "string",
  "entityId": "string",
  "detail": "string (diff JSON ngắn)",
  "timestamp": "Date"
}
```

## Truy vấn nhiều bảng (không có JOIN)
- Dashboard cần Device kèm metric mới nhất + alert đang mở → query riêng từng collection rồi merge ở service layer (Spring Data MongoDB `Aggregation` với `$lookup` nếu cần gộp tại DB, dùng có giới hạn vì `$lookup` không phải JOIN SQL thật).
- Pagination dùng `Pageable` của Spring Data MongoDB (vẫn giữ được convention `Link` header như JHipster sinh ra cho REST).

## Service generate
- JHipster vẫn generate repository/REST resource/DTO cho MongoDB (JHipster hỗ trợ Mongo native qua `--db mongodb` lúc tạo entity), CRUD Angular tự sinh không đổi.
- `service Device, AlertRule, AlertEvent with serviceImpl` — giữ nguyên định hướng tách service layer như JDL gốc.

## Seed dữ liệu mẫu
- MongoDB không có Liquibase changelog — seed bằng 1 trong 2 cách: (1) script `mongosh` chạy khi container `mongo` khởi tạo lần đầu (`docker-entrypoint-initdb.d`), hoặc (2) `CommandLineRunner`/`ApplicationRunner` trong Spring Boot kiểm tra collection rỗng rồi insert seed (5 Device gNodeB/AMF/SMF/UPF/UDM, vài AlertRule mặc định).
- Khuyến nghị dùng cách (2) để seed đi theo code, dễ review qua git.

## Liên quan
- `domain/device.md`, `domain/metric.md`, `domain/alert.md`, `domain/topology.md`, `domain/audit.md` — chi tiết nghiệp vụ từng entity.
- `database/indexing.md` — index cho các field truy vấn nhiều.
