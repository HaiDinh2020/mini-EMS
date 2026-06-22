# Architecture

## Sơ đồ tổng thể

```
┌─────────────────────────────────────────────────────┐
│                   Angular SPA                        │
│  Dashboard │ Device List │ Alert │ Topology │ Audit  │
└────────────────────┬────────────────────────────────┘
                     │ HTTP REST + WebSocket STOMP
┌────────────────────▼────────────────────────────────┐
│               Spring Boot (JHipster)                 │
│                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │ REST Layer   │  │ Service Layer│  │ AOP Audit │ │
│  │ (Resources)  │  │ (Business)   │  │ (Aspect)  │ │
│  └──────┬───────┘  └──────┬───────┘  └─────┬─────┘ │
│         │                 │                 │        │
│  ┌──────▼─────────────────▼─────────────────▼─────┐ │
│  │              Repository Layer (Spring Data)     │ │
│  └──────────────────────┬──────────────────────────┘ │
│                          │                            │
│  ┌───────────────┐  ┌────▼──────┐                   │
│  │  Scheduler    │  │  MongoDB  │                   │
│  │  (Collector)  │  └───────────┘                   │
│  └───────┬───────┘                                  │
│          │ SSH/TCP                                   │
│  ┌───────▼──────────────────────────────────┐       │
│  │  Network Devices (SSH-accessible nodes)  │       │
│  └──────────────────────────────────────────┘       │
│                                                      │
│  ┌───────────────────────────────────────────────┐  │
│  │  WebSocket Broker (STOMP)                     │  │
│  │  /topic/device-status   /topic/alerts         │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

## Sequence: Collector Flow

```
Scheduler → DeviceCollectorService.collectAll()
    │
    ├─► TCP-connect(ip, sshPort)
    │       ├── timeout/refused → status=OFFLINE, skip SSH
    │       └── success → status=REACHABLE
    │
    ├─► SSHClient.connect() → run commands
    │       ├── /proc/stat (CPU)
    │       ├── free -m (RAM)
    │       └── df -h / (Disk)
    │
    ├─► MetricSampleRepository.save(sample)
    │
    ├─► AlertEngineService.evaluate(device, sample)
    │       ├── so khớp AlertRule enabled
    │       ├── tạo/cập nhật AlertEvent
    │       └── WebSocketBroker.publish("/topic/alerts", event)
    │
    └─► DeviceRepository.save(device) ← cập nhật status, lastCheckedAt
```

## Layers & Packages

```
com.miniems/
├── domain/          ← MongoDB @Document entities
├── repository/      ← Spring Data Mongo repositories
├── service/         ← business logic (interfaces + impl)
├── web/rest/        ← REST controllers (@RestController)
├── security/        ← JWT config, UserDetailsService
├── collector/       ← DeviceCollectorService, SSH parser
├── alert/           ← AlertEngineService
├── websocket/       ← STOMP config, MessageBroker
└── audit/           ← AuditAspect (@Aspect)
```

## ADR – Architecture Decision Records

### ADR-01: Monolith thay vì Microservice
- **Quyết định**: Monolith JHipster (Spring Boot + Angular cùng 1 app)
- **Lý do**: Phạm vi bài toán vừa (~10 entity), 1 dev, mục tiêu demo. Microservice thêm độ phức tạp (service discovery, API gateway, distributed tracing) không mang lại giá trị thực tế ở quy mô này.
- **Trade-off chấp nhận**: Khó scale độc lập từng module nếu sau này cần; giải quyết bằng cách tách package rõ ràng để refactor dễ khi cần.

### ADR-02: MongoDB thay vì PostgreSQL
- **Quyết định**: MongoDB làm database chính
- **Lý do**: `MetricSample` là dữ liệu time-series tăng nhanh, schema có thể biến đổi theo loại thiết bị; MongoDB linh hoạt hơn, dễ lưu embedded document (metrics theo device), index TTL tự dọn data cũ.
- **Trade-off chấp nhận**: Mất khả năng JOIN phức tạp; giải quyết bằng aggregation pipeline hoặc query riêng từng collection.

### ADR-03: JWT thay vì OAuth2/Keycloak
- **Quyết định**: JWT stateless built-in của JHipster
- **Lý do**: Không cần SSO, không cần external IdP. Keycloak thêm 1 service nặng (~512MB RAM) không cần thiết cho bài demo. JWT đủ để làm RBAC Admin/User.
- **Trade-off chấp nhận**: Token không revoke được trước khi hết hạn; giải quyết bằng thời gian expire ngắn + refresh token.

### ADR-04: TCP-connect thay vì ICMP Ping
- **Quyết định**: Kiểm tra reachability bằng cách mở TCP socket tới cổng SSH (22)
- **Lý do**: ICMP ping trong container Docker yêu cầu `CAP_NET_RAW` hoặc chạy privileged — không an toàn, không hoạt động trên nhiều CI environment.
- **Trade-off chấp nhận**: Một thiết bị có thể ping được nhưng SSH port đóng sẽ bị báo OFFLINE; chấp nhận được vì mục tiêu cuối cùng là SSH để lấy metric.
