# Current Task

## Sprint hiện tại: Sprint 5 – Bonus

### Mục tiêu Sprint 5

Hoàn thiện các tính năng bonus: Topology view tương tác (vis-network / d3-force), tích hợp Prometheus + Grafana scrape JVM metrics và device metrics, và 5 container giả lập 5G Core (gNodeB, AMF, SMF, UPF, UDM) xuất hiện đầy đủ trong hệ thống.

**Tiền đề – Sprint 4 đã xong:**

- `DashboardResource` `GET /api/dashboard/summary` (totalDevices, online, offline, unknown, openAlerts, critical, warning)
- `GET /api/metric-samples/devices/{id}/metrics/history` (from/to/limit, sắp xếp tăng dần)
- `DashboardComponent`: 4 summary card + WS subscribe `/topic/device-status` + `/topic/alerts`
- `MetricChartComponent`: chart.js line chart (CPU/RAM/Disk), timeRange 1h/6h/24h, threshold lines
- `AlertNotificationService`: toast queue + bell badge realtime
- `AlertEventListComponent`: filter status/severity, Acknowledge realtime, WS row update
- `AdminAuditLogs` (`/admin/audit-logs`): filter + pagination server-side, ADMIN-only

---

## TODO ngay (theo thứ tự)

### 1. Topology View

#### Backend

- [x] `TopologyResource` (`GET /api/topology/devices` + `GET /api/topology/links`) — `@PreAuthorize` đúng
- [x] `DeviceSeederMigration` (Mongock order 003): seed 5 5G device (gNodeB/AMF/SMF/UPF/UDM, IP 172.28.0.11–15, sshPort=2022, no credential)
- [x] `TopologyLinkSeederMigration` (Mongock order 004): seed 4 link (N2, N11, N4, N8)
- [x] `DeviceCollectorService`: TCP-reachable + no credential → ONLINE (thay vì UNKNOWN)

#### Frontend

- [x] `vis-network` installed (`npm install vis-network`)
- [x] Route `/topology` (lazy-loaded) trong `app.routes.ts`
- [x] `TopologyComponent`: forkJoin devices+links → vis-network, WS realtime update màu, dblclick → navigate
- [x] Link "Topology" trong navbar (`fa-icon project-diagram`)

**Done khi:** Mở `/topology` thấy đồ thị 5 node 5G Core nối với nhau; màu node thay đổi khi collector cập nhật status.

---

### 2. Prometheus + Grafana

#### Backend

- [x] `spring-boot-starter-actuator` + `micrometer-registry-prometheus` đã có trong `pom.xml`
- [x] `/management/prometheus` đã `permitAll` trong `SecurityConfiguration`
- [x] `DeviceCollectorService`: inject `MeterRegistry`, gauge `ems.device.online.count` + `ems.device.offline.count` (cập nhật sau mỗi scan)

#### Infrastructure

- [x] `src/main/docker/prometheus/prometheus.yml`: target `app:8080`, path `/management/prometheus`
- [x] `src/main/docker/grafana/provisioning/datasources/datasource.yml`: URL `http://prometheus:9090`
- [x] `src/main/docker/grafana/provisioning/dashboards/ems-dashboard.json`: stat cards (online/offline/JVM heap/threads) + time-series (device count, heap, HTTP rate)
- [x] `src/main/docker/app.yml`: service `prometheus` + `grafana` tích hợp vào Docker network `ems-net`

**Done khi:** `docker compose up -d` → `http://localhost:9090` thấy metric `ems_device_online_count`; `http://localhost:3000` thấy dashboard EMS.

---

### 3. 5G Core Containers (giả lập)

#### docker-compose

- [x] 5 service trong `app.yml` (network `ems-net`, subnet `172.28.0.0/24`):
  - `gnodeb` (172.28.0.11), `amf` (172.28.0.12), `smf` (172.28.0.13), `upf` (172.28.0.14), `udm` (172.28.0.15)
  - Image: `alpine:3.20`, command: `sh -c 'while true; do nc -l -p 2022; sleep 0.1; done'`
- [x] `DeviceSeederMigration` seed device với sshPort=2022, no credential
- [x] Collector: TCP-reachable + no credential → ONLINE

**Done khi:** Tất cả 5 node 5G Core xuất hiện ONLINE trên Dashboard và Topology sau `docker compose up -d`.

---

### 4. Sprint 5 review checklist

- [x] `GET /api/topology/devices` + `GET /api/topology/links` trả dữ liệu đúng
- [x] `TopologyComponent` vis-network: node màu theo status, WS realtime, dblclick navigate
- [x] Link "Topology" trong navbar
- [x] `/management/prometheus` expose metric `ems_device_online_count`, `ems_device_offline_count`
- [x] Prometheus scrape target `app:8080` (Docker network)
- [x] Grafana dashboard tự động provisioning (datasource + dashboard JSON)
- [x] 5 container 5G Core (`alpine+nc`), fixed IP 172.28.0.11–15, TCP port 2022
- [x] Topology seed: gNodeB→AMF→SMF→UPF + AMF→UDM (Mongock order 004)
- [x] `docker compose -f src/main/docker/app.yml up -d` khởi động toàn bộ
- [ ] `./mvnw test` pass (chạy thực tế để xác nhận)

---

## Blocked / Cần quyết định

- [x] Thư viện topology: `**vis-network`** (2026-06-27)
- [x] 5G container giả lập: `**alpine` + `nc -lk**` (2026-06-27)
- [x] Custom Prometheus metric: `**MeterRegistry` trực tiếp** (2026-06-27)

---

## Upcoming (Sprint 6 – Hardening)

- E2E test (Cypress hoặc Playwright): luồng login → dashboard → alert acknowledge
- Security review: HTTP headers, CSRF, JWT expiry, HTTPS redirect
- README.md hoàn chỉnh: hướng dẫn setup, architecture diagram, screenshots
- Demo script: `docker compose up -d` → walkthrough 5 tính năng chính

---

## Ghi chú / Quyết định đã thống nhất


| Ngày       | Quyết định                                                                                                                                                                                                                                                                                                                                                            |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| –          | MongoDB thay PostgreSQL (ADR-02)                                                                                                                                                                                                                                                                                                                                      |
| –          | TCP-connect thay ICMP ping (ADR-04)                                                                                                                                                                                                                                                                                                                                   |
| –          | Monolith, không microservice (ADR-01)                                                                                                                                                                                                                                                                                                                                 |
| 2026-06-22 | Sprint 0 done: JHipster + JDL + git + docker-compose skeleton                                                                                                                                                                                                                                                                                                         |
| 2026-06-22 | Package base: `com.vht.ems`                                                                                                                                                                                                                                                                                                                                           |
| 2026-06-22 | MongoDB **7.0-community**, dev dùng Docker MongoDB                                                                                                                                                                                                                                                                                                                    |
| 2026-06-22 | Seed mechanism: `ApplicationRunner` (cách 2 trong `database/jdl.md`)                                                                                                                                                                                                                                                                                                  |
| 2026-06-22 | IP seed 5G nodes: subnet `172.28.0.0/24`, nodes `.11`–`.15`                                                                                                                                                                                                                                                                                                           |
| 2026-06-23 | Sprint 1 done: Device/Credential CRUD, Jasypt, @PreAuthorize, seed 5 node 5G Core                                                                                                                                                                                                                                                                                     |
| 2026-06-24 | Sprint 2 done: `DeviceCollectorService` (sshj + TCP check + @Scheduled 60s + thread pool `collector-`), `MetricParser` (pure fn, 9 unit tests pass), `MetricSample.deviceId` indexed, jasypt-spring-boot-starter, endpoint `/api/devices/{id}/metrics`                                                                                                                |
| 2026-06-24 | Sprint 3 done: AlertRule RBAC + seed, AlertEvaluatorService (tạo/update/resolve AlertEvent + WS broadcast), AlertEventResource (GET list/detail/acknowledge), AuditAspect (AOP @AfterReturning), AuditLogResource (admin-only), WebSocket STOMP `/topic/device-status` + `/topic/alerts`                                                                              |
| 2026-06-24 | Sprint 4 done: DashboardResource `/api/dashboard/summary`, MetricHistory endpoint, DashboardComponent (4 cards + WS), MetricChartComponent (chart.js line chart, 1h/6h/24h), AlertNotificationService + bell badge, AlertEvent list (filter + WS realtime row update), AdminAuditLogs (/admin/audit-logs, filter+pagination), navbar Dashboard link + Audit Logs link |
| 2026-06-27 | Sprint 5 bắt đầu: Topology view, Prometheus/Grafana, 5G Core containers                                                                                                                                                                                                                                                                                               |
| 2026-06-27 | Topology: `vis-network`; 5G sim: `alpine + nc -lk`; Prometheus custom metric: `MeterRegistry` trực tiếp                                                                                                                                                                                                                                                               |
| 2026-06-27 | Sprint 5 done: TopologyResource, DeviceSeeder+TopologyLinkSeeder (Mongock 003/004), vis-network TopologyComponent, MeterRegistry gauges, app.yml all-in-one (prometheus+grafana+5G nodes)                                                                                                                                                                             |


