# Domain: Security

## Trách nhiệm
Xác thực (JWT), phân quyền (RBAC), bảo vệ Credential, CORS, và các quy tắc bảo mật chung.

## Authentication: JWT

JHipster generate sẵn toàn bộ JWT pipeline. Cấu hình quan trọng:

```yaml
# application.yml
jhipster:
  security:
    authentication:
      jwt:
        base64-secret: ${JWT_SECRET}   # lấy từ env, không hardcode
        token-validity-in-seconds: 86400          # 24h
        token-validity-in-seconds-for-remember-me: 2592000  # 30 days
```

## Roles & Permissions

| Role | Quyền |
|---|---|
| `ROLE_ADMIN` | Toàn bộ CRUD: Device, Credential, AlertRule, TopologyLink |
| `ROLE_USER` | Read-only: Device list, Dashboard, AlertEvent, Topology |

### Backend – Method-level Security

```java
// Chỉ ADMIN mới tạo/sửa/xóa
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@PostMapping("/api/devices")
public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody DeviceDTO dto) { ... }

// Cả 2 role đều đọc được
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
@GetMapping("/api/devices")
public ResponseEntity<List<DeviceDTO>> getAllDevices(Pageable pageable) { ... }
```

### Frontend – Route Guard

```typescript
// Angular route config
{
  path: 'device/new',
  component: DeviceUpdateComponent,
  canActivate: [UserRouteAccessService],
  data: { authorities: ['ROLE_ADMIN'] }
},
{
  path: 'devices',
  component: DeviceListComponent,
  canActivate: [UserRouteAccessService],
  data: { authorities: ['ROLE_USER', 'ROLE_ADMIN'] }
}
```

## Credential Security

### Mã hóa tại rest (Jasypt)

```java
@Document(collection = "credentials")
public class Credential {
    @Id
    private String id;
    private String name;
    private AuthType authType;      // PASSWORD, SSH_KEY
    private String username;
    private String encryptedSecret; // Jasypt encrypted, KHÔNG BAO GIỜ trả qua API
}
```

```java
@Service
public class CredentialService {
    private final StandardPBEStringEncryptor encryptor;

    // Lưu: encrypt trước khi save
    public Credential save(CredentialDTO dto) {
        credential.setEncryptedSecret(encryptor.encrypt(dto.getPlainSecret()));
        return credentialRepository.save(credential);
    }

    // Dùng: decrypt chỉ trong RAM, chỉ cho SSHClient
    public String decryptSecret(String credentialId) {
        Credential c = credentialRepository.findById(credentialId).orElseThrow();
        return encryptor.decrypt(c.getEncryptedSecret()); // không log, không trả API
    }
}
```

### CredentialDTO – không expose secret

```java
public class CredentialDTO {
    private String id;
    private String name;
    private AuthType authType;
    private String username;
    // plainSecret: chỉ nhận vào khi tạo/update, không bao giờ trả ra
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String plainSecret;
    // encryptedSecret: không có trong DTO
}
```

## CORS Configuration

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // Cấu hình cho profile prod:
        // allowedOrigins = ${ALLOWED_ORIGINS}  ← từ env var
        // KHÔNG dùng allowedOrigins("*") khi production
    }
}
```

## Secrets Management

```
# .env (không commit git)
JWT_SECRET=<base64-encoded-secret-min-512bits>
JASYPT_ENCRYPTOR_PASSWORD=<strong-password>
MONGO_PASSWORD=<db-password>
ALLOWED_ORIGINS=https://yourdomain.com
```

```yaml
# application.yml
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USER}:${MONGO_PASSWORD}@mongodb:27017/miniems
```

## Security Checklist
- [ ] JWT secret từ env var, độ dài ≥ 512 bits (base64)
- [ ] Jasypt encryptor password từ env var
- [ ] `@PreAuthorize` trên mọi endpoint write
- [ ] Angular route guard trên mọi page admin
- [ ] CORS whitelist cụ thể, không `*`
- [ ] CredentialDTO không expose `encryptedSecret`
- [ ] Logger không bao giờ in SSH credential hay JWT secret
- [ ] `.env` trong `.gitignore`
- [ ] HTTPS khi deploy (TLS termination tại reverse proxy)

## Liên kết
- `domain/audit.md` – AuditLog ghi hành động CRUD nhưng không log secret
- `backend/collector.md` – SSHClient dùng `CredentialService.decryptSecret()` trong RAM
- `backend/deployment.md` – env vars trong docker-compose
