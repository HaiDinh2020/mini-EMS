# Kế hoạch phát triển: Mini EMS & Infrastructure Inventory
**Stack:** JHipster (Spring Boot + Angular), PostgreSQL, Docker Compose

---

## 1. Quyết định kiến trúc

| Hạng mục | Lựa chọn | Lý do |
|---|---|---|
| JHipster app type | **Monolith** (Spring Boot + Angular cùng 1 app) | Phạm vi bài toán vừa, không cần chia microservice/gateway, deploy đơn giản, đúng tinh thần "1 lệnh docker-compose" |
| Database | **PostgreSQL** | Quan hệ rõ (Device – Metric – Alert – Audit), JHipster hỗ trợ native, dễ làm reporting/threshold query |
| Auth | **JWT** (built-in JHipster) | Không cần Keycloak/OAuth2 server riêng, vẫn đủ RBAC Admin/User qua `Authority` |
| Realtime | **WebSocket STOMP** (module có sẵn của JHipster) | JHipster generate sẵn `websocket-configuration.ts`/`WebsocketSecurityConfiguration`, chỉ cần thêm topic nghiệp vụ |
| Audit | **Spring Boot Actuator Audit Events** (có sẵn, bảng `jhi_persistent_audit_event`) + 1 entity `AuditLog` tự viết cho hành động CRUD nghiệp vụ | Audit login/logout JHipster tự làm; audit CRUD Device/AlertRule cần log riêng theo nghiệp vụ |
| Thu thập dữ liệu | **Spring `@Scheduled` job** dùng TCP-connect check (thay ICMP ping) + SSH client (`sshj`) | Tránh phải cấp `CAP_NET_RAW` cho container khi ping ICMP thật |

---

## 2. Mô hình dữ liệu (JDL)

```jdl
entity Device {
  name String required
  ipAddress String required
  hostname String
  deviceType DeviceType required
  vendor String
  model String
  sshPort Integer
  sshUsername String
  location String
  status DeviceStatus
  lastCheckedAt Instant
  monitoringEnabled Boolean
  description String
}

enum DeviceType {
  SERVER, ROUTER, SWITCH, FIREWALL, GNODEB, AMF, SMF, UPF, UDM, OTHER
}

enum DeviceStatus {
  ONLINE, OFFLINE, UNKNOWN, WARNING, CRITICAL
}

entity Credential {
  name String
  authType AuthType
  username String
  encryptedSecret String
}

enum AuthType {
  PASSWORD, SSH_KEY
}

entity MetricSample {
  cpuUsage Float
  ramUsage Float
  diskUsage Float
  pingLatencyMs Float
  collectedAt Instant required
}

enum MetricType {
  CPU, RAM, DISK, PING_LATENCY, AVAILABILITY
}

entity AlertRule {
  metricType MetricType required
  thresholdWarning Float
  thresholdCritical Float
  enabled Boolean
}

entity AlertEvent {
  metricType MetricType
  value Float
  severity Severity
  message String
  triggeredAt Instant
  resolvedAt Instant
  status AlertStatus
}

enum Severity { WARNING, CRITICAL }
enum AlertStatus { OPEN, ACKNOWLEDGED, RESOLVED }

entity TopologyLink {
  linkType String
  bandwidthMbps Integer
  status LinkStatus
}

enum LinkStatus { UP, DOWN, DEGRADED }

entity AuditLog {
  username String
  action String
  entityName String
  entityId String
  detail String
  timestamp Instant
}

relationship ManyToOne {
  Device{credential} to Credential
  MetricSample{device} to Device
  AlertEvent{device} to Device
  AlertEvent{rule} to AlertRule
  TopologyLink{sourceDevice} to Device
  TopologyLink{targetDevice} to Device
}

paginate Device, MetricSample, AlertEvent, AuditLog with pagination
service Device, AlertRule, AlertEvent with serviceImpl
```

JHipster sẽ tự sinh entity, repository, REST resource, Liquibase changelog, và toàn bộ màn hình CRUD Angular cho các entity trên — chỉ còn việc tự viết logic nghiệp vụ (collector, alert engine, websocket, topology, audit AOP).

---

## 3. Các module cần code tay (ngoài phần auto-generate)

### 3.1 Collector Service (Ping/SSH)
- `DeviceCollectorService` chạy `@Scheduled(fixedDelayString = "${ems.collector.interval-ms}")`.
- Reachability check: mở TCP socket tới IP:port (vd cổng SSH 22) thay vì ICMP — tránh phải chạy container privileged.
- Nếu reachable → SSH bằng thư viện **sshj**, chạy các lệnh Linux chuẩn:
  - CPU: `top -bn1 | grep "Cpu(s)"` hoặc đọc `/proc/stat` hai lần cách nhau 1s để tính % chính xác.
  - RAM: `free -m` rồi parse `used/total`.
  - Disk: `df -h /`.
  - Uptime: `cat /proc/uptime`.
- Ghi `MetricSample`, cập nhật `Device.status`, `lastCheckedAt`.
- Parser cho từng lệnh viết thành hàm thuần (pure function) để **unit test** dễ với input mẫu (text output cố định của `free -m`, `top`).

### 3.2 Alert Engine
- Sau mỗi `MetricSample`, so khớp với `AlertRule` đang `enabled` theo `metricType`.
- Vượt `thresholdWarning` → tạo/giữ `AlertEvent` `WARNING`; vượt `thresholdCritical` → `CRITICAL`; về dưới ngưỡng → tự `RESOLVED`.
- Ví dụ ngưỡng mặc định: RAM ≥ 80% = WARNING, ≥ 90% = CRITICAL; CPU ≥ 85% = WARNING.
- Mỗi lần tạo/đổi trạng thái alert → publish lên WebSocket topic `/topic/alerts`.

### 3.3 RBAC
- Dùng `Authority` có sẵn của JHipster: `ROLE_ADMIN`, `ROLE_USER`.
- `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` cho: tạo/sửa/xóa Device, Credential, AlertRule.
- `ROLE_USER`: chỉ xem Dashboard, Device list, Alert list, Topology (read-only).

### 3.4 Audit Log
- Viết 1 `@Aspect` (Spring AOP) bắt các method `create/update/delete` trong `DeviceResource`, `AlertRuleResource`, `CredentialResource`.
- Ghi vào entity `AuditLog`: user hiện tại (`SecurityUtils.getCurrentUserLogin()`), action, entityName, entityId, detail (diff JSON ngắn), timestamp.
- Trang Admin riêng để xem Audit Log (filter theo user/action/thời gian).

### 3.5 Dashboard & Realtime
- Trang dashboard Angular: card tổng Online/Offline/Warning/Critical, bảng danh sách Device có badge trạng thái, biểu đồ CPU/RAM theo thời gian (Chart.js/`ng2-charts`) cho từng Device.
- Subscribe STOMP topic `/topic/device-status` và `/topic/alerts` để cập nhật real-time không cần reload.

### 3.6 Topology View (bonus)
- Dùng `vis-network` hoặc `d3-force` trong Angular component riêng.
- Node = Device (màu theo status), Edge = `TopologyLink`.
- Click vào node mở panel chi tiết (metric mới nhất, alert đang mở).

### 3.7 Mô phỏng mạng 5G Core (bonus) — điểm hay nhất của bài
Theo gợi ý đề bài "coi mỗi thứ là một node mạng bình thường": dựng 5 container nhỏ trong docker-compose, mỗi container:
- Base image Alpine + `openssh-server` + `stress-ng` (hoặc script python random load).
- Đặt tên: `node-gnodeb`, `node-amf`, `node-smf`, `node-upf`, `node-udm`.
- `stress-ng --cpu 1 --cpu-load <random>%` chạy định kỳ để CPU dao động thật, tạo dữ liệu sống động cho dashboard/alert mà không cần phần cứng 5G thật.
- Add các Device này vào DB qua Liquibase seed data với `deviceType = GNODEB/AMF/SMF/UPF/UDM`, hệ thống EMS coi chúng như server bình thường — collector SSH vào lấy CPU/RAM y như mọi node khác.

### 3.8 Prometheus/Grafana (bonus)
- JHipster đã expose Micrometer metrics tại `/management/prometheus`.
- Thêm container `prometheus` scrape endpoint trên + container `grafana` import dashboard JVM/HTTP có sẵn của JHipster (`jhipster ci-cd`/monitoring subgenerator có template sẵn).
- Tuỳ chọn: đăng ký thêm Micrometer Gauge theo từng Device (cpu/ram) trong `DeviceCollectorService` để Grafana vẽ được lịch sử theo thiết bị, không chỉ JVM của app.

---

## 4. Lộ trình Sprint (gợi ý 5–6 tuần, 1 dev)

| Sprint | Thời lượng | Nội dung |
|---|---|---|
| 0 – Setup | 2–3 ngày | `jhipster` init monolith JWT + Angular + Postgres; `jhipster jdl entities.jdl`; khởi tạo docker-compose, git, CI cơ bản |
| 1 – CRUD & RBAC | 1 tuần | Hoàn thiện CRUD Device/Credential (mã hoá secret bằng Jasypt), gắn `@PreAuthorize` theo role, seed data mẫu |
| 2 – Collector | 1 tuần | TCP reachability check, SSH client + parser CPU/RAM/Disk, scheduled job, unit test parser |
| 3 – Alert + Audit + Realtime | 1 tuần | Alert engine theo ngưỡng, AlertRule CRUD, AOP AuditLog, WebSocket broadcast |
| 4 – Dashboard UI | 1 tuần | Trang dashboard, biểu đồ metric, alert banner, trang Audit Log (Admin) |
| 5 – Bonus | 1–1.5 tuần | Topology view, Prometheus/Grafana, 5 container giả lập 5G Core, hoàn thiện docker-compose 1 lệnh |
| 6 – Hardening | 3–4 ngày | E2E test (Cypress sẵn của JHipster), review security, README hướng dẫn chạy, demo |

---

## 5. Phác thảo Docker Compose

```yaml
services:
  postgresql:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: miniems
  mini-ems-app:
    build: .
    depends_on: [postgresql]
    ports: ["8080:8080"]
  prometheus:
    image: prom/prometheus
    volumes: ["./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml"]
  grafana:
    image: grafana/grafana
    ports: ["3000:3000"]
  node-gnodeb:
    build: ./network-sim
    environment: { NODE_NAME: gNodeB }
  node-amf:
    build: ./network-sim
    environment: { NODE_NAME: AMF }
  node-smf:
    build: ./network-sim
    environment: { NODE_NAME: SMF }
  node-upf:
    build: ./network-sim
    environment: { NODE_NAME: UPF }
  node-udm:
    build: ./network-sim
    environment: { NODE_NAME: UDM }
```
(`./network-sim` = Dockerfile chung: Alpine + openssh-server + stress-ng, chỉ khác biến môi trường tên node.)

---

## 6. Rủi ro & lưu ý kỹ thuật
- **Không dùng ICMP ping thật trong container** trừ khi compose có `cap_add: NET_RAW` — ưu tiên TCP-connect check để đơn giản, ổn định trên mọi môi trường CI/Docker.
- **Bảo mật Credential**: không log plaintext password/private key; mã hoá tại rest (Jasypt) và chỉ decrypt trong RAM lúc gọi SSH.
- **Tải job thu thập**: nếu số Device lớn, scheduled job nên chạy bất đồng bộ (thread pool riêng) để không chặn nhau khi 1 SSH timeout.
- **JHipster auto-gen UI**: tận dụng tối đa CRUD Angular tự sinh, chỉ custom phần Dashboard/Topology/Alert để tiết kiệm thời gian.

---

## 7. Đối chiếu kế hoạch với tiêu chí đánh giá

### 7.1 Thiết kế hệ thống
- Vẽ kèm 1 sơ đồ kiến trúc tổng thể (Angular SPA → REST API → Service layer → Repository → PostgreSQL; Scheduler/Collector; WebSocket broker; AOP Audit) và 1 sequence diagram cho luồng collector (Schedule → Ping/SSH → lưu Metric → đánh giá Alert → push WebSocket).
- Viết ngắn 1 ADR (Architecture Decision Record) giải thích lý do chọn monolith thay vì microservice, JWT thay vì OAuth2/Keycloak, PostgreSQL thay vì MongoDB — thể hiện có suy nghĩ trade-off, không chỉ "theo mặc định".
- Tách layer rõ theo chuẩn JHipster: `domain` / `repository` / `service` / `web.rest` / `security`, không để logic nghiệp vụ lẫn trong Controller.

### 7.2 Chất lượng code
- Unit test cho phần logic thuần (parser `free -m`, `top`, alert threshold evaluator) bằng JUnit5 + Mockito; integration test dùng Testcontainers PostgreSQL.
- Dùng DTO + MapStruct (JHipster tự sinh) để không expose entity JPA trực tiếp ra API.
- Exception xử lý tập trung qua `ExceptionTranslator` có sẵn của JHipster (trả lỗi chuẩn RFC 7807), không try-catch rải rác.
- Logging có cấu trúc bằng SLF4J, tuyệt đối không log secret/SSH credential.
- Git: commit nhỏ theo feature, message rõ nghĩa, có README hướng dẫn chạy local & docker.

### 7.3 API
- REST chuẩn resource-based, dùng đúng HTTP verb, pagination/sort theo convention JHipster (`Link` header).
- Tận dụng OpenAPI/Swagger tự sinh (springdoc) để có sẵn tài liệu/test thử API mà không cần viết tay.
- Trả lỗi nhất quán theo Problem Details; validate input bằng Bean Validation (`@NotNull`, `@Pattern` cho IP/hostname).
- Tài liệu hoá riêng các WebSocket topic (`/topic/device-status`, `/topic/alerts`) kèm cấu trúc payload, vì đây không nằm trong OpenAPI.

### 7.4 Database
- Schema đã chuẩn hoá theo JDL ở mục 2; thêm index cho các cột truy vấn nhiều: `device.ip_address`, `metric_sample.collected_at`, `alert_event.status`.
- Liquibase changelog version hoá theo từng entity (JHipster tự sinh khi `jhipster jdl`), không sửa tay schema đã apply.
- Lưu ý dữ liệu `MetricSample` tăng nhanh theo thời gian (time-series) → thêm job dọn/archive dữ liệu cũ, hoặc nêu rõ hướng mở rộng dùng TimescaleDB nếu cần scale.
- Có kế hoạch backup tối thiểu: `pg_dump` định kỳ ra volume riêng.

### 7.5 Dashboard
- UX đủ 3 trạng thái: loading / empty / error, không chỉ happy-path.
- Hiển thị theo role: Admin thấy thêm nút quản lý AlertRule/Credential, User chỉ xem.
- Cập nhật realtime qua WebSocket thay vì polling, có biểu đồ lịch sử CPU/RAM theo Device.
- Responsive cơ bản (Bootstrap có sẵn của JHipster) để demo trên nhiều kích thước màn hình.

### 7.6 Security
- JWT + BCrypt password hashing (JHipster default), RBAC cả ở method-level (`@PreAuthorize`) và route guard Angular (`UserRouteAccessService`), không chỉ ẩn UI mà còn chặn ở API.
- Mã hoá `Credential.encryptedSecret` (Jasypt hoặc tương đương) tại rest, decrypt chỉ trong RAM lúc gọi SSH, không bao giờ trả secret qua API/log.
- CORS cấu hình rõ origin cho phép, không để `*` khi deploy.
- Audit Log ghi đủ ai-làm-gì-khi-nào nhưng không chứa dữ liệu nhạy cảm.
- Secrets (DB password, JWT secret) truyền qua biến môi trường/`.env`, không hardcode trong `application.yml` commit lên git.

### 7.7 Deployment
- `docker compose up -d` chạy được toàn bộ stack từ máy sạch, có healthcheck (`/management/health`) cho service chính trước khi container "healthy".
- Dockerfile multi-stage (build Angular + Spring Boot jar rồi mới copy vào image runtime nhẹ) để giảm kích thước image.
- Tách cấu hình theo Spring profile `dev`/`prod`, không dùng `dev` config khi chạy compose production-like.
- README liệt kê đầy đủ biến môi trường, port, lệnh build/run, và cách seed dữ liệu mẫu để người review thấy ngay dashboard có dữ liệu.

## 8. Definition of Done
- [ ] `docker compose up -d` chạy được toàn bộ hệ thống từ máy sạch.
- [ ] CRUD Device + Credential hoạt động, phân quyền Admin/User đúng.
- [ ] Collector tự ping/SSH định kỳ, cập nhật status & metric.
- [ ] Alert sinh ra khi vượt ngưỡng CPU/RAM, tự resolve khi hết.
- [ ] Dashboard cập nhật realtime qua WebSocket.
- [ ] Audit Log ghi nhận đầy đủ hành động CRUD theo user.
- [ ] (Bonus) Topology view hiển thị node + link theo status.
- [ ] (Bonus) Prometheus/Grafana lên dashboard JVM + metric thiết bị.
- [ ] (Bonus) 5 node gNodeB/AMF/SMF/UPF/UDM xuất hiện trên dashboard như node mạng bình thường.
