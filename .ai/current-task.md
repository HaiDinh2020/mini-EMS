# Current Task

## Sprint hiện tại: Sprint 3 – Alert + Audit + WebSocket

### Mục tiêu Sprint 3
Triển khai Alert Engine (so ngưỡng → sinh AlertEvent, tự resolve), CRUD AlertRule có RBAC, AOP AuditLog ghi nhận mọi thao tác CRUD nghiệp vụ, và WebSocket STOMP để push realtime device-status / alert tới Dashboard.

**Tiền đề:** Sprint 2 đã xong (TCP reachability, SSH collector, parser CPU/RAM/Disk, `@Scheduled` mỗi 60s, lưu `MetricSample`, cập nhật `Device.status` + `lastCheckedAt`).

---

## TODO ngay (theo thứ tự)

### 1. AlertRule – CRUD + RBAC
Tham chiếu: `.ai/domain/alert.md`

**Backend**
- [x] Kiểm tra entity `AlertRule` đã đúng spec (`deviceId` nullable, `enabled`, `thresholdWarning`, `thresholdCritical`, `metricType`)
- [x] `AlertRuleResource`: áp `@PreAuthorize` đúng spec
  - GET (list, detail): `hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')`
  - POST, PUT, DELETE: `hasAuthority('ROLE_ADMIN')`
- [x] Repository: thêm query `findEnabledRulesForDevices(List<String> deviceIds)` (MongoDB `$or` query)
- [x] Seed 3 `AlertRule` mặc định qua `AlertRuleSeederMigration` (Mongock order 002):
  - CPU: warning 85%, critical 95%
  - RAM: warning 80%, critical 90%
  - Disk: warning 75%, critical 90%

**Frontend**
- [x] Trang `alert-rule` list: ẩn nút Create/Edit/Delete khi không có `ROLE_ADMIN` (`*jhiHasAnyAuthority`)
- [x] Route guard: `new/edit` yêu cầu `['ROLE_ADMIN']`

**Done khi:** Admin tạo/sửa/xóa AlertRule; User chỉ xem.

---

### 2. Alert Engine – `AlertEvaluatorService`
Tham chiếu: `.ai/domain/alert.md`

**Backend**
- [ ] Tạo `AlertEvaluatorService.evaluate(device, metricSample)`:
  - Lấy danh sách `AlertRule` enabled cho device (device-specific + global)
  - So sánh value với `thresholdCritical` → `thresholdWarning`
  - Gọi `createOrUpdate(device, rule, severity, value)` hoặc `autoResolve(device, rule)`
- [ ] `createOrUpdate` logic:
  - Tìm `AlertEvent` đang OPEN theo `(deviceId, ruleId)`
  - Nếu chưa có → tạo mới, set `status=OPEN`, `triggeredAt=now()`
  - Nếu có, severity thay đổi → update severity
  - Nếu có, severity giống → chỉ update `value` + `triggeredAt`
  - Sau khi save → broadcast `/topic/alerts` qua `SimpMessagingTemplate`
- [ ] `autoResolve` logic:
  - Tìm `AlertEvent` OPEN theo `(deviceId, ruleId)` 
  - Nếu tồn tại → set `status=RESOLVED`, `resolvedAt=now()`, save + broadcast
- [ ] Thêm query trong `AlertEventRepository`:
  ```java
  Optional<AlertEvent> findByDeviceIdAndRuleIdAndStatus(String deviceId, String ruleId, AlertStatus status);
  List<AlertEvent> findByDeviceIdAndStatus(String deviceId, AlertStatus status);
  long countByStatusAndSeverity(AlertStatus status, Severity severity);
  ```
- [ ] `DeviceCollectorService` gọi `alertEvaluatorService.evaluate(device, metricSample)` sau khi lưu MetricSample

**Test**
- [ ] Unit test `AlertEvaluatorService`: value vượt critical → tạo AlertEvent CRITICAL; value giảm về bình thường → auto-resolve; severity đổi từ WARNING → CRITICAL → update đúng

**Done khi:** Collector chạy, metric vượt ngưỡng → AlertEvent tự sinh; metric hạ xuống → tự resolve.

---

### 3. AlertEvent – REST API + Acknowledge
Tham chiếu: `.ai/domain/alert.md`

**Backend**
- [ ] `AlertEventResource`:
  - `GET /api/alert-events`: filter theo `status`, `severity`, `deviceId` (dùng query params)
  - `GET /api/alert-events/{id}`: chi tiết
  - `PUT /api/alert-events/{id}/acknowledge`: chuyển `OPEN → ACKNOWLEDGED` (auth: USER)
- [ ] `@PreAuthorize`:
  - GET: `hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')`
  - PUT acknowledge: `hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')`
  - Không expose POST/DELETE (AlertEvent chỉ do engine sinh)
- [ ] Payload WebSocket khi broadcast:
  ```json
  {
    "type": "ALERT_EVENT",
    "alertEventId": "...",
    "deviceId": "...",
    "deviceName": "...",
    "metricType": "RAM",
    "severity": "CRITICAL",
    "value": 91.5,
    "message": "RAM usage 91.5% exceeds critical threshold 90%",
    "status": "OPEN",
    "timestamp": "..."
  }
  ```

**Frontend**
- [ ] Trang `alert-event` list: hiển thị status badge (OPEN=đỏ, ACKNOWLEDGED=vàng, RESOLVED=xanh), filter theo status/severity
- [ ] Nút "Acknowledge" trong list/detail (USER và ADMIN đều dùng được)

**Done khi:** User thấy danh sách alert; bấm Acknowledge → badge chuyển màu tức thì.

---

### 4. AOP AuditLog
Tham chiếu: `.ai/domain/audit.md`

**Backend**
- [ ] Tạo `AuditAspect` (`@Aspect @Component`) trong `com.vht.ems.aop`:
  - Pointcut bắt `create*`, `update*`, `delete*` trong `DeviceResource`, `AlertRuleResource`, `CredentialResource`
  - Dùng `@AfterReturning` để lấy kết quả trả về
  - Lấy `username` từ `SecurityUtils.getCurrentUserLogin()`
  - Map method name → action (`CREATE` / `UPDATE` / `DELETE`)
  - Build `detail` JSON ngắn từ args/result — **KHÔNG chứa** `encryptedSecret`, SSH private key, password plaintext
  - Lưu `AuditLog` qua `AuditLogRepository`
- [ ] `AuditLogResource` (chỉ ADMIN):
  - `GET /api/admin/audit-logs`: pagination, filter `username`, `action`, `entityName`, `from`, `to` (dùng `MongoTemplate + Criteria`)
  - Không expose PUT/DELETE (append-only)
- [ ] `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` cho toàn bộ `AuditLogResource`

**Test**
- [ ] Unit/integration test: gọi `POST /api/devices` → `audit_logs` có 1 document với `action=CREATE`, `entityName=Device`, đúng `username`
- [ ] Xác nhận: gọi `POST /api/credentials` → `detail` KHÔNG chứa field secret

**Done khi:** Mọi CRUD Device/Credential/AlertRule đều sinh AuditLog; Admin xem được qua API.

---

### 5. WebSocket STOMP – topic nghiệp vụ
Tham chiếu: `.ai/backend/websocket.md`

**Backend**
- [ ] Inject `SimpMessagingTemplate` vào `AlertEvaluatorService` và `DeviceCollectorService`
- [ ] `DeviceCollectorService` publish `/topic/device-status` sau cập nhật:
  ```json
  { "deviceId": "...", "name": "node-amf", "status": "ONLINE", "lastCheckedAt": "..." }
  ```
- [ ] `AlertEvaluatorService` publish `/topic/alerts` sau create/update/resolve AlertEvent (payload theo spec trên)
- [ ] **Không** gọi `SimpMessagingTemplate` từ Controller — chỉ gọi từ service layer
- [ ] Không tạo topic riêng cho Credential / AuditLog

**Frontend**
- [ ] Service `WebsocketService` (hoặc tận dụng service JHipster có sẵn): subscribe `/topic/device-status` và `/topic/alerts` khi vào Dashboard
- [ ] Unsubscribe khi rời trang (tránh memory leak)
- [ ] Khi nhận message `/topic/device-status`: cập nhật status badge của device tương ứng trong danh sách (không reload trang)
- [ ] Khi nhận message `/topic/alerts`: hiển thị toast/banner alert mới hoặc cập nhật badge số alert đang OPEN

**Done khi:** Không cần reload, status device + alert cập nhật ngay khi collector chạy xong.

---

### 6. Sprint 3 review checklist
- [ ] `./mvnw test` pass (bao gồm unit test AlertEvaluatorService + AuditAspect)
- [ ] Collector chạy → metric vượt ngưỡng → AlertEvent CRITICAL xuất hiện trong `GET /api/alert-events`
- [ ] AlertEvent tự RESOLVED khi metric hạ về bình thường
- [ ] User bấm Acknowledge → status chuyển đúng
- [ ] CRUD Device/AlertRule/Credential → AuditLog có record tương ứng
- [ ] `GET /api/admin/audit-logs?action=CREATE` → trả về đúng danh sách
- [ ] WebSocket: mở Dashboard, collector chạy → status device cập nhật không reload
- [ ] AuditLog: không lộ secret trong field `detail`
- [ ] Swagger phản ánh đúng endpoint `/api/alert-rules`, `/api/alert-events`, `/api/admin/audit-logs`

---

## Blocked / Cần quyết định
- [ ] AlertEvent acknowledge: chỉ user tạo request hay bất kỳ USER/ADMIN? (đề xuất: bất kỳ USER/ADMIN)
- [ ] Khi device bị DELETE, AlertEvent liên quan xử lý thế nào? (đề xuất: giữ nguyên, deviceId vẫn ghi lịch sử)

---

## Upcoming (Sprint 4 – Dashboard UI)
- Dashboard Angular: tổng số device online/offline/unknown
- Biểu đồ metric CPU/RAM/Disk theo thời gian (Chart.js / ngx-charts)
- Alert banner realtime + badge đếm OPEN alerts
- Trang Admin xem Audit Log với filter/pagination
- Trang Topology (nếu Sprint 5 chưa làm)

---

## Ghi chú / Quyết định đã thống nhất
| Ngày | Quyết định |
|---|---|
| – | MongoDB thay PostgreSQL (ADR-02) |
| – | TCP-connect thay ICMP ping (ADR-04) |
| – | Monolith, không microservice (ADR-01) |
| 2026-06-22 | Sprint 0 done: JHipster + JDL + git + docker-compose skeleton |
| 2026-06-22 | Package base: `com.vht.ems` |
| 2026-06-22 | MongoDB **7.0-community**, dev dùng Docker MongoDB |
| 2026-06-22 | Seed mechanism: `ApplicationRunner` (cách 2 trong `database/jdl.md`) |
| 2026-06-22 | IP seed 5G nodes: subnet `172.28.0.0/24`, nodes `.11`–`.15` |
| 2026-06-23 | Sprint 1 done: Device/Credential CRUD, Jasypt, @PreAuthorize, seed 5 node 5G Core |
| 2026-06-23 | Sprint 2 done: TCP reachability, SSH collector (sshj), parser CPU/RAM/Disk, @Scheduled 60s, MetricSample time-series |
| 2026-06-23 | Sprint 3 done: AlertRule RBAC + seed, AlertEvaluatorService, AlertEvent acknowledge, AuditAspect, WebSocket STOMP config |
