# Current Task

## Sprint hiện tại: Sprint 0 – Setup

### Mục tiêu Sprint 0
Khởi tạo project, cấu hình MongoDB, dựng docker-compose skeleton, đảm bảo `docker compose up -d` chạy được app trống.

---

## TODO ngay (theo thứ tự)

### 1. Khởi tạo JHipster project
```bash
mkdir mini-ems && cd mini-ems
jhipster
# Chọn: Monolith, Angular, JWT, MongoDB, Maven
```
Cấu hình quan trọng khi JHipster hỏi:
- App type: `Monolith`
- Frontend: `Angular`
- Auth: `JWT`
- Database: `MongoDB`
- Dev DB: `MongoDB` (embedded hoặc Docker)

### 2. Viết JDL và generate entities
- Xem file `database/jdl.md` để lấy JDL đã adapt sang MongoDB
- Chạy: `jhipster jdl entities.jdl`

### 3. Cấu hình docker-compose
- Xem `backend/deployment.md` để lấy template docker-compose đầy đủ
- Tạo `./network-sim/Dockerfile` cho 5 node giả lập

### 4. Khởi tạo git
```bash
git init
echo ".env" >> .gitignore
echo "application-prod.yml" >> .gitignore
git add . && git commit -m "chore: jhipster init with MongoDB"
```

### 5. Smoke test
- `./mvnw` → app build OK
- `docker compose up -d` → app + MongoDB lên, `/management/health` trả `UP`

---

## Blocked / Cần quyết định
- [ ] Version MongoDB: 6.x hay 7.x? → ưu tiên 7.0-community
- [ ] Embedded MongoDB cho dev test hay chạy Docker MongoDB luôn?
- [ ] Tên package base: `com.miniems` hay theo tên project khác?

---

## Upcoming (Sprint 1)
- Hoàn thiện CRUD Device với validation IP/hostname
- Mã hoá `Credential.encryptedSecret` bằng Jasypt
- Gắn `@PreAuthorize` theo role
- Seed data mẫu: 5 device (gNodeB, AMF, SMF, UPF, UDM) + 1 admin user

---

## Ghi chú / Quyết định đã thống nhất
| Ngày | Quyết định |
|---|---|
| – | MongoDB thay PostgreSQL (xem ADR-02 trong architecture.md) |
| – | TCP-connect thay ICMP ping (xem ADR-04) |
| – | Monolith, không microservice (xem ADR-01) |
