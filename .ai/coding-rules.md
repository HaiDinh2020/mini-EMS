# Coding Rules

## Nguyên tắc chung
- **1 class = 1 trách nhiệm** – không để logic nghiệp vụ trong Controller
- **Không expose entity MongoDB trực tiếp ra API** – luôn dùng DTO + MapStruct
- **Không try-catch rải rác** – tập trung xử lý lỗi qua `ExceptionTranslator` (JHipster built-in, RFC 7807)
- **Không hardcode secret** – mọi password/key qua biến môi trường
- **Không log secret** – tuyệt đối không log SSH credential, JWT secret, DB password

## Naming Convention

### Java
```
DeviceDocument          ← MongoDB @Document (domain layer)
DeviceDTO               ← Data Transfer Object (API layer)
DeviceMapper            ← MapStruct interface
DeviceRepository        ← Spring Data Mongo
DeviceService           ← interface (service layer)
DeviceServiceImpl       ← implementation
DeviceResource          ← REST controller (@RestController)
```

### MongoDB Collections
```
devices                 ← snake_case, plural
metric_samples
alert_rules
alert_events
topology_links
audit_logs
```

### REST Endpoints
```
GET    /api/devices
POST   /api/devices
GET    /api/devices/{id}
PUT    /api/devices/{id}
DELETE /api/devices/{id}
GET    /api/devices/{id}/metrics   ← metrics của 1 device
```

### WebSocket Topics
```
/topic/device-status    ← { deviceId, status, timestamp }
/topic/alerts           ← { alertEventId, deviceId, severity, message, timestamp }
```

## Logging
```java
// Dùng SLF4J, không dùng System.out.println
private static final Logger log = LoggerFactory.getLogger(DeviceCollectorService.class);

// Level guidelines:
// INFO  – bắt đầu/kết thúc job, device thay đổi status
// DEBUG – chi tiết SSH command output (không log khi prod)
// WARN  – không kết nối được device, retry
// ERROR – exception không recover được
```

## Testing

### Unit Test (JUnit 5 + Mockito)
- Bắt buộc với: SSH output parser, Alert threshold evaluator
- Dùng static input string giả lập output của `free -m`, `top`, `df -h`
```java
@Test
void parseRamUsage_shouldReturn75WhenUsed3GOf4G() {
    String freeOutput = "Mem:           4096       3072       1024";
    float result = RamParser.parse(freeOutput);
    assertThat(result).isEqualTo(75.0f);
}
```

### Integration Test
- Dùng `@DataMongoTest` + Flapdoodle Embedded MongoDB (hoặc Testcontainers MongoDB)
- Test repository queries, aggregation pipeline

### E2E
- Cypress (JHipster built-in) cho happy path: login → xem dashboard → CRUD device

## Security Rules
- `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` bắt buộc cho mọi endpoint write (POST/PUT/DELETE)
- Angular route guard `UserRouteAccessService` cho từng page
- CORS: cấu hình rõ allowed origin, **không để `*`** khi deploy production
- Input validation: `@NotNull`, `@Pattern` cho IP address và hostname

## Git
- Commit nhỏ theo feature, không commit nhiều feature cùng lúc
- Message format: `feat(device): add TCP reachability check`
- Branch: `feature/<sprint>-<description>`, ví dụ `feature/s2-collector-ssh`
- Không commit `.env`, `application-prod.yml` có chứa secret

## Code Quality Checklist trước khi merge
- [ ] Không có `System.out.println` hay `e.printStackTrace()`
- [ ] Không để `TODO` chưa xử lý
- [ ] DTO tách biệt entity, có `@Valid` trên request body
- [ ] Unit test cho logic phức tạp (parser, alert eval)
- [ ] Swagger/OpenAPI annotation đầy đủ trên Resource class
