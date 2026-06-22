# Current Task

## Sprint hiện tại: Sprint 1 – CRUD & RBAC

### Mục tiêu Sprint 1
Hoàn thiện CRUD Device/Credential theo domain spec, bảo mật secret bằng Jasypt, phân quyền Admin/User đúng spec, seed dữ liệu demo 5 node 5G Core.

**Tiền đề:** Sprint 0 đã xong (JHipster init, JDL entities, git, docker-compose skeleton, smoke test).

---

## TODO ngay (theo thứ tự)

### 1. Device – validation & business defaults
Tham chiếu: `.ai/domain/device.md`

**Backend**
- [ ] Thêm `@Pattern` IPv4 cho `ipAddress` trên `Device` + `DeviceDTO`:
  ```java
  @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")
  ```
- [ ] Trong `DeviceServiceImpl.save()`: set default nếu null
  - `status = UNKNOWN`
  - `monitoringEnabled = true`
  - `sshPort = 22`
- [ ] Unique `ipAddress`: thêm `existsByIpAddress` trong `DeviceRepository`, throw `BadRequestAlertException` khi trùng
- [ ] `DeviceDTO`: chỉ trả `credentialId`, không embed full `Credential` (cập nhật `DeviceMapper` nếu cần)

**Frontend**
- [ ] Form create/edit: validate IP client-side (pattern tương tự)
- [ ] Ẩn/disable nút Create/Edit/Delete cho user không có `ROLE_ADMIN`

**Test**
- [ ] `DeviceResourceIT`: POST IP invalid → 400; POST IP trùng → 400; POST hợp lệ → defaults đúng

**Done khi:** Admin tạo device qua UI/API với IP sai bị reject; device mới có status UNKNOWN.

---

### 2. Credential – Jasypt encrypt/decrypt
Tham chiếu: `.ai/domain/security.md`

**Backend**
- [ ] Thêm dependency Jasypt (`jasypt-spring-boot-starter` hoặc `org.jasypt:jasypt`)
- [ ] Cấu hình `StandardPBEStringEncryptor` bean, password từ env `JASYPT_ENCRYPTOR_PASSWORD`
- [ ] Refactor `CredentialDTO`:
  - Xóa `encryptedSecret` khỏi response
  - Thêm `plainSecret` với `@JsonProperty(access = WRITE_ONLY)`
- [ ] `CredentialServiceImpl`: encrypt trước khi save, decrypt qua method `decryptSecret(id)` (chỉ dùng nội bộ, không expose API)
- [ ] Không log `plainSecret` / decrypted value

**Frontend**
- [ ] Form credential: field "Secret" (password input), không hiển thị encrypted value
- [ ] List/detail: bỏ cột/field `encryptedSecret`

**Test**
- [ ] `CredentialResourceIT`: response GET không chứa secret; POST với plainSecret → DB lưu dạng encrypted

**Done khi:** API không bao giờ trả secret; decrypt chỉ qua service method nội bộ.

---

### 3. RBAC – `@PreAuthorize` backend + route guard frontend
Tham chiếu: `.ai/domain/security.md`, `.ai/domain/device.md`

**Backend** — áp dụng cho `DeviceResource`, `CredentialResource`, `AlertRuleResource`, `TopologyLinkResource`:

| Method | Annotation |
|---|---|
| GET (list, detail) | `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")` |
| POST, PUT, PATCH, DELETE | `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` |

- [ ] Dùng `AuthoritiesConstants.ADMIN` / `USER` thay vì string literal (nhất quán với `UserResource`)
- [ ] Bật `@EnableMethodSecurity` nếu chưa có

**Frontend**
- [ ] `device.routes.ts`, `credential.routes.ts`: thêm `data: { authorities: [...] }`
  - List/view: `['ROLE_USER', 'ROLE_ADMIN']`
  - new/edit: `['ROLE_ADMIN']`
- [ ] Ẩn action buttons trong list component theo role (`HasAnyAuthorityDirective`)

**Test**
- [ ] Integration test: USER gọi POST `/api/devices` → 403; GET → 200

**Done khi:** User thường chỉ đọc được; Admin full CRUD.

---

### 4. Seed data mẫu
Tham chiếu: `.ai/database/jdl.md`, `.ai/backend/deployment.md`

- [ ] Tạo `config/DataSeeder.java` implements `ApplicationRunner` (profile `dev` + `prod` hoặc flag `EMS_SEED_ENABLED=true`)
- [ ] Chỉ seed khi collection `device` rỗng (idempotent)
- [ ] Seed nội dung:
  - 1 `Credential` (authType PASSWORD, secret encrypt qua Jasypt)
  - 5 `Device`: gNodeB, AMF, SMF, UPF, UDM — IP nội bộ Docker (`172.28.0.11`–`.15`), `deviceType` tương ứng, gắn credential
  - (Optional) 2–3 `AlertRule` mặc định (CPU/RAM warning 70%, critical 90%)
- [ ] Admin user: JHipster đã có — document default login trong README (`admin` / `admin`)

**Done khi:** App khởi động lần đầu → `/api/devices` trả 5 device 5G Core.

---

### 5. Sprint 1 review checklist
- [ ] `./mvnw test` pass
- [ ] Login admin → CRUD device/credential OK
- [ ] Login user → chỉ xem list, không tạo/sửa/xóa
- [ ] Swagger `/swagger-ui` phản ánh đúng auth requirement
- [ ] Cập nhật README: env vars (`JASYPT_ENCRYPTOR_PASSWORD`, `JWT_SECRET`, `SPRING_MONGODB_URI`), default accounts

---

## Blocked / Cần quyết định
- [ ] Cascade delete Device → MetricSample/AlertEvent: hard delete hay soft delete? (đề xuất: hard delete cho Sprint 1)

---

## Upcoming (Sprint 2 – Collector)
- TCP reachability check (không ICMP — ADR-04)
- SSH client (sshj) lấy CPU/RAM/Disk
- Parser pure function + unit test
- `@Scheduled` collector job mỗi 60s (`ems.collector.interval-ms`)
- Cập nhật `Device.status` + `lastCheckedAt`
- Lưu `MetricSample` time-series

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
