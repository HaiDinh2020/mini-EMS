# Current Task

## Sprint hiện tại: Sprint 4 – Dashboard UI

### Mục tiêu Sprint 4
Xây dựng Dashboard Angular hiển thị tổng quan hệ thống realtime: summary card device online/offline/unknown, biểu đồ metric CPU/RAM/Disk theo thời gian, alert banner + badge đếm OPEN alerts qua WebSocket, trang Alert Event list/acknowledge, và trang Admin xem Audit Log với filter/pagination.

**Tiền đề – Sprint 3 đã xong:**
- `AlertRule` entity + `AlertRuleResource` với `@PreAuthorize` đúng RBAC
- `AlertRuleRepository.findEnabledRulesForDevices(List<String> deviceIds)` (MongoDB `$or`)
- `AlertRuleSeederMigration` (Mongock order 002): seed 3 rule CPU/RAM/Disk
- Frontend `alert-rule` list: ẩn Create/Edit/Delete khi không có `ROLE_ADMIN`
- `AlertEvaluatorService.evaluate(device, metricSample)`: tạo/update/resolve AlertEvent + broadcast `/topic/alerts`
- `AlertEventRepository` với query `findByDeviceIdAndRuleIdAndStatus`, `findByDeviceIdAndStatus`, `countByStatusAndSeverity`
- `DeviceCollectorService` gọi `alertEvaluatorService.evaluate()` sau mỗi vòng quét
- `AlertEventResource`: GET list (filter status/severity/deviceId), GET detail, PUT acknowledge
- `AuditAspect` (`@AfterReturning`): bắt `create*`/`update*`/`delete*` trong Device/AlertRule/Credential resource
- `AuditLogResource` (`GET /api/admin/audit-logs`): pagination + filter, chỉ ADMIN
- `WebsocketConfiguration` + `WebsocketSecurityConfiguration` (STOMP over SockJS)
- `DeviceCollectorService` publish `/topic/device-status` sau mỗi cập nhật
- `AlertEvaluatorService` publish `/topic/alerts` sau create/update/resolve

---

## TODO ngay (theo thứ tự)

### 1. Dashboard – Summary Cards
Tham chiếu: `.ai/frontend/dashboard.md`

**Backend**
- [x] Endpoint `GET /api/dashboard/summary`:
  ```json
  {
    "totalDevices": 5,
    "online": 3,
    "offline": 1,
    "unknown": 1,
    "openAlerts": 2,
    "criticalAlerts": 1,
    "warningAlerts": 1
  }
  ```
  - Auth: `hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')`
  - Tổng hợp từ `DeviceRepository.countByStatus(status)` + `AlertEventRepository.countByStatusAndSeverity(...)`

**Frontend**
- [x] Route `/dashboard` (lazy-loaded)
- [x] Component `DashboardComponent` với 4 card:
  - **Online** (xanh lá) – count devices ONLINE
  - **Offline** (đỏ) – count devices OFFLINE
  - **Unknown** (xám) – count devices UNKNOWN
  - **Open Alerts** (cam) – count AlertEvent OPEN, chia WARNING / CRITICAL
- [x] Subscribe WebSocket `/topic/device-status` → reload summary
- [x] Subscribe WebSocket `/topic/alerts` → reload summary
- [x] Unsubscribe khi rời trang

**Done khi:** Mở Dashboard thấy đủ 4 card, số liệu đúng; khi collector chạy, card cập nhật không reload.

---

### 2. Biểu đồ Metric – CPU / RAM / Disk theo thời gian

**Backend**
- [x] Endpoint `GET /api/devices/{id}/metrics/history`:
  - Query params: `from`, `to` (ISO-8601), `limit` (default 50)
  - Trả về `List<MetricSampleDTO>` sắp xếp tăng dần theo `collectedAt`
  - Auth: `hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')`

**Frontend**
- [x] Cài thư viện biểu đồ: `chart.js` (dùng trực tiếp, tương thích Angular 21)
- [x] Component `MetricChartComponent` (dùng lại được):
  - Input: `deviceId`, `metricType` (CPU/RAM/DISK), timeRange toggle 1h/6h/24h
  - Fetch `GET /api/metric-samples/devices/{id}/metrics/history` khi mount + khi timeRange thay đổi
  - Line chart: trục X = HH:mm, trục Y = % usage
  - Kẻ đường ngang threshold WARNING + CRITICAL nếu truyền vào
- [x] Tích hợp 3 biểu đồ (CPU / RAM / Disk) vào trang `Device Detail`

**Done khi:** Mở trang Device Detail → thấy 3 chart CPU/RAM/Disk; đổi timeRange → chart cập nhật.

---

### 3. Alert Banner Realtime + Badge

**Frontend**
- [x] Service `AlertNotificationService`:
  - Subscribe `/topic/alerts` (dùng lại `WebsocketService`)
  - Giữ signal `toasts[]` chứa tối đa 5 alert gần nhất chưa dismiss
- [x] Badge đỏ trên icon bell (NavBar) hiện số OPEN alerts; cập nhật ngay khi `/topic/alerts` push
- [x] Badge count tổng hợp từ:
  1. `GET /api/dashboard/summary` khi init (giá trị khởi tạo)
  2. WebSocket push (delta realtime)

**Done khi:** Có alert mới → toast hiện ngay, badge tăng; reload trang → badge đúng với DB.

---

### 4. Trang Alert Event List + Acknowledge

**Frontend**
- [x] Route `/alert-event` (đã có từ JHipster)
- [x] `AlertEventListComponent` enhanced:
  - Filter dropdown: status (OPEN/ACKNOWLEDGED/RESOLVED) + severity (CRITICAL/WARNING)
  - Nút **Acknowledge** (chỉ hiện khi status = OPEN): update row ngay, không reload
  - Realtime: WS `/topic/alerts` → cập nhật status/severity row tương ứng
  - Phân trang (JHipster paginator)

**Done khi:** User thấy danh sách alert; bấm Acknowledge → badge chuyển vàng tức thì; khi alert tự resolve → badge chuyển xanh qua WebSocket.

---

### 5. Trang Admin – Audit Log

**Frontend**
- [x] Route `/admin/audit-logs` (lazy-loaded, inherit ADMIN guard từ `/admin`)
- [x] Component `AdminAuditLogs`:
  - Fetch `GET /api/admin/audit-logs` với filter: `username`, `action`, `entityName`, `from`, `to`
  - Hiển thị bảng: Timestamp, Username, Action badge (CREATE=xanh, UPDATE=vàng, DELETE=đỏ), Entity, Detail (expandable JSON)
  - Phân trang server-side
- [x] Link "Audit Logs" trong menu Admin (navbar)

**Done khi:** Admin vào `/admin/audit-logs`, lọc theo `action=CREATE` → thấy danh sách đúng; User bị redirect (403 / route guard).

---

### 6. Sprint 4 review checklist
- [ ] Dashboard load: 4 summary card hiển thị đúng số liệu từ DB
- [ ] WebSocket: collector chạy → card Online/Offline cập nhật không reload
- [ ] WebSocket: metric vượt ngưỡng → badge tăng
- [ ] Chart CPU/RAM/Disk trên Device Detail hiển thị đúng dữ liệu lịch sử
- [ ] Đổi timeRange (1h / 6h / 24h) → chart cập nhật đúng
- [ ] Alert Event list: filter status/severity hoạt động; Acknowledge đổi badge ngay
- [ ] Audit Log: Admin truy cập được; User bị chặn (route guard + 403)
- [ ] Audit Log filter `username` + `action` + `from/to` hoạt động đúng
- [ ] Không memory leak WebSocket (unsubscribe khi rời trang)
- [ ] `./mvnw test` pass (không regression từ Sprint 3)

---

## Blocked / Cần quyết định
- [x] Thư viện chart: **`ng2-charts`** (đã chọn, 2026-06-24)
- [ ] Biểu đồ tổng hợp trên Dashboard (optional) – làm Sprint 4 hay để Sprint 5?

---

## Upcoming (Sprint 5 – Bonus)
- Topology view (vis-network / d3-force): node = device, link = TopologyLink, màu theo status
- Prometheus + Grafana: scrape `/actuator/prometheus`, dashboard JVM + device metric
- 5 container giả lập 5G Core (gNodeB, AMF, SMF, UPF, UDM) trong `docker-compose.yml`

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
| 2026-06-24 | Sprint 2 done: `DeviceCollectorService` (sshj + TCP check + @Scheduled 60s + thread pool `collector-`), `MetricParser` (pure fn, 9 unit tests pass), `MetricSample.deviceId` indexed, jasypt-spring-boot-starter, endpoint `/api/devices/{id}/metrics` |
| 2026-06-24 | Sprint 3 done: AlertRule RBAC + seed, AlertEvaluatorService (tạo/update/resolve AlertEvent + WS broadcast), AlertEventResource (GET list/detail/acknowledge), AuditAspect (AOP @AfterReturning), AuditLogResource (admin-only), WebSocket STOMP `/topic/device-status` + `/topic/alerts` |
| 2026-06-24 | Sprint 4 done: DashboardResource `/api/dashboard/summary`, MetricHistory endpoint, DashboardComponent (4 cards + WS), MetricChartComponent (chart.js line chart, 1h/6h/24h), AlertNotificationService + bell badge, AlertEvent list (filter + WS realtime row update), AdminAuditLogs (/admin/audit-logs, filter+pagination), navbar Dashboard link + Audit Logs link |
