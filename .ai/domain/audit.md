# Domain: Audit

## Trách nhiệm
Ghi nhận mọi hành động CRUD nghiệp vụ (Device, Credential, AlertRule) với thông tin: ai làm, làm gì, khi nào, trên entity nào. Audit Log chỉ được ghi thêm (append-only), không sửa/xóa.

## Hai lớp Audit

| Lớp | Nguồn | Mô tả |
|---|---|---|
| System audit | `jhi_persistent_audit_event` (JHipster built-in) | Login/logout, session events |
| Business audit | `AuditLog` (tự viết) | CRUD Device, Credential, AlertRule |

File này chỉ mô tả lớp **Business Audit**.

## MongoDB Document

```java
@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private String id;

    @Indexed
    private String username;

    private String action;          // "CREATE", "UPDATE", "DELETE"
    private String entityName;      // "Device", "AlertRule", "Credential"

    @Indexed
    private String entityId;

    private String detail;          // JSON diff ngắn (trước/sau), không chứa secret
    private Instant timestamp;
}
```

## AOP Aspect

```java
@Aspect
@Component
public class AuditAspect {

    // Bắt tất cả method create*/update*/delete* trong các Resource class
    @AfterReturning(
        pointcut = "execution(* com.miniems.web.rest.DeviceResource.create*(..)) || " +
                   "execution(* com.miniems.web.rest.DeviceResource.update*(..)) || " +
                   "execution(* com.miniems.web.rest.DeviceResource.delete*(..)) || " +
                   "execution(* com.miniems.web.rest.AlertRuleResource.*(..)) || " +
                   "execution(* com.miniems.web.rest.CredentialResource.*(..))",
        returning = "result"
    )
    public void logAction(JoinPoint joinPoint, Object result) {
        String username = SecurityUtils.getCurrentUserLogin().orElse("system");
        String methodName = joinPoint.getSignature().getName();
        String action = resolveAction(methodName);  // CREATE/UPDATE/DELETE
        String entityName = resolveEntity(joinPoint);
        String entityId = extractEntityId(result);

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDetail(buildDetail(joinPoint, result)); // KHÔNG chứa encryptedSecret
        log.setTimestamp(Instant.now());

        auditLogRepository.save(log);
    }
}
```

## DTO

```java
public class AuditLogDTO {
    private String id;
    private String username;
    private String action;
    private String entityName;
    private String entityId;
    private String detail;
    private Instant timestamp;
}
```

## REST API

| Method | Path | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/admin/audit-logs` | ADMIN | Danh sách, pagination |
| GET | `/api/admin/audit-logs?username=&action=&from=&to=` | ADMIN | Filter theo user/action/thời gian |

## Query Patterns

```java
// Filter đa tiêu chí (dùng MongoTemplate + Criteria)
Criteria criteria = new Criteria();
if (username != null) criteria.and("username").is(username);
if (action != null) criteria.and("action").is(action);
if (from != null) criteria.and("timestamp").gte(from);
if (to != null) criteria.and("timestamp").lte(to);

Query query = new Query(criteria).with(pageable);
return mongoTemplate.find(query, AuditLog.class);
```

## Quy tắc bảo mật trong Audit
- `detail` field **KHÔNG** chứa: encryptedSecret, SSH private key, password dạng plaintext
- Với Credential, chỉ ghi: `{ "name": "prod-server-key", "authType": "SSH_KEY", "username": "root" }` — bỏ qua field `encryptedSecret`
- Audit Log là **append-only**: không expose endpoint DELETE/PUT

## Data Retention
- Giữ audit log tối thiểu **90 ngày**
- Không dùng TTL index (khác với MetricSample) — audit log cần giữ lâu hơn
- Cân nhắc archive ra file sau 1 năm nếu volume lớn

## Liên kết
- AuditAspect intercept: `DeviceResource`, `AlertRuleResource`, `CredentialResource`
- `SecurityUtils.getCurrentUserLogin()` lấy từ Spring Security context (JWT)
- Trang Admin xem Audit Log: `frontend/dashboard.md`
