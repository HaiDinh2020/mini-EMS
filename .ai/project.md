# Mini EMS – Project Overview

## Mục tiêu
Xây dựng hệ thống EMS (Element Management System) thu nhỏ để giám sát thiết bị mạng (server, router, switch, 5G Core nodes) qua SSH/TCP, hiển thị dashboard realtime, sinh alert tự động, và audit mọi thao tác CRUD.

## Stack
| Layer | Công nghệ |
|---|---|
| Backend | Spring Boot (JHipster monolith) |
| Frontend | Angular (cùng 1 app với Spring Boot) |
| Database | **MongoDB** |
| Auth | JWT (built-in JHipster) |
| Realtime | WebSocket STOMP |
| Infra | Docker Compose |

## Phạm vi tính năng

### Core
- Quản lý Device + Credential (CRUD, RBAC)
- Collector: TCP reachability check + SSH lấy CPU/RAM/Disk
- Alert engine: so ngưỡng, tự resolve
- Dashboard realtime (WebSocket)
- Audit Log (AOP)

### Bonus
- Topology view (vis-network / d3-force)
- Prometheus + Grafana monitoring
- 5 container giả lập 5G Core (gNodeB, AMF, SMF, UPF, UDM)

## Nguyên tắc kiến trúc
- **1 lệnh deploy**: `docker compose up -d` là đủ
- **Monolith**: không tách microservice – phạm vi bài toán vừa, đơn giản hóa vận hành
- **MongoDB** thay PostgreSQL: linh hoạt schema cho MetricSample (time-series), dễ mở rộng field theo device type
- **Không ICMP ping**: dùng TCP-connect để tránh `CAP_NET_RAW` trong container
- **Không hardcode secret**: mọi credential qua biến môi trường / `.env`

## Cấu trúc tài liệu `.ai/`
```
.ai/
├── project.md          ← file này
├── architecture.md     ← sơ đồ tổng thể, ADR
├── coding-rules.md     ← quy tắc code, naming, test
├── current-task.md     ← sprint hiện tại, TODO ngay
├── domain/             ← 1 file = 1 domain
├── backend/            ← collector, websocket, deployment
├── database/           ← schema MongoDB, indexing
└── frontend/           ← dashboard, topology
```

## Lộ trình Sprint

| Sprint | Thời lượng | Nội dung chính |
|---|---|---|
| 0 – Setup | 2–3 ngày | JHipster init, cấu hình MongoDB, git, docker-compose skeleton |
| 1 – CRUD & RBAC | 1 tuần | Device/Credential CRUD, @PreAuthorize, seed data |
| 2 – Collector | 1 tuần | TCP check, SSH client, parser CPU/RAM/Disk, @Scheduled |
| 3 – Alert + Audit + WS | 1 tuần | Alert engine, AlertRule CRUD, AOP AuditLog, WebSocket |
| 4 – Dashboard UI | 1 tuần | Dashboard Angular, biểu đồ metric, alert banner |
| 5 – Bonus | 1–1.5 tuần | Topology, Prometheus/Grafana, 5G sim containers |
| 6 – Hardening | 3–4 ngày | E2E test, security review, README, demo |

## Definition of Done
- [ ] `docker compose up -d` chạy được từ máy sạch
- [ ] CRUD Device + Credential, phân quyền Admin/User đúng
- [ ] Collector tự SSH định kỳ, cập nhật status & metric
- [ ] Alert sinh khi vượt ngưỡng, tự resolve khi hết
- [ ] Dashboard cập nhật realtime qua WebSocket
- [ ] Audit Log ghi nhận đầy đủ hành động CRUD
- [ ] (Bonus) Topology view node + link theo status
- [ ] (Bonus) Prometheus/Grafana dashboard JVM + metric thiết bị
- [ ] (Bonus) 5 node 5G Core xuất hiện trên dashboard
