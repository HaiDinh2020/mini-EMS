# Backend Domain: Deployment

## Mục tiêu
`docker compose up -d` chạy được toàn bộ hệ thống từ máy sạch, có healthcheck, không cần bước thủ công nào thêm.

## Thành phần trong Docker Compose
| Service | Vai trò |
|---|---|
| `mongo` | Database chính (thay PostgreSQL) — xem `database/jdl.md` |
| `mini-ems-app` | Spring Boot + Angular monolith, build từ Dockerfile multi-stage |
| `prometheus` | Scrape `/management/prometheus` của app (bonus) |
| `grafana` | Hiển thị dashboard JVM/HTTP + metric thiết bị (bonus) |
| `node-gnodeb`, `node-amf`, `node-smf`, `node-upf`, `node-udm` | Container giả lập 5G Core, chung Dockerfile `./network-sim` (Alpine + openssh-server + stress-ng), phân biệt qua biến môi trường `NODE_NAME` |

## Dockerfile chính (mini-ems-app)
- **Multi-stage build**:
  1. Stage build Angular (`npm run build`).
  2. Stage build Spring Boot jar (Maven/Gradle), copy artifact Angular vào `src/main/resources/static` trước khi package.
  3. Stage runtime: base image JRE nhẹ (vd `eclipse-temurin:21-jre-alpine`), chỉ copy jar đã build, không mang theo toolchain build.
- Mục đích: giảm kích thước image cuối, không lộ source code build tools trong image production.

## Healthcheck
- Dùng endpoint `/management/health` (Spring Boot Actuator) làm healthcheck cho `mini-ems-app` trong `docker-compose.yml`.
- Service phụ thuộc (nếu có) nên chờ `mini-ems-app` ở trạng thái "healthy" trước khi coi là sẵn sàng demo.
- `mongo` cũng nên có healthcheck riêng (`mongosh --eval "db.adminCommand('ping')"`) để app không start trước khi DB sẵn sàng.

## Spring Profile
- Tách rõ `dev` và `prod`:
  - `dev`: log SQL/query verbose (nếu cần), CORS mở rộng cho local Angular dev server.
  - `prod`: log gọn, CORS giới hạn origin cụ thể (không dùng `*`), không bật DevTools.
- Compose dùng profile `prod`-like khi build image release; không để mặc định `dev` chạy trong container compose.

## Biến môi trường & secrets
- DB connection string, JWT secret, thông tin nhạy cảm khác truyền qua biến môi trường / file `.env`, **không hardcode** trong `application.yml` rồi commit lên git.
- README phải liệt kê đầy đủ: biến môi trường cần có, port expose, lệnh build/run, cách seed dữ liệu mẫu.

## Container giả lập 5G Core (network-sim)
- 1 Dockerfile dùng chung cho 5 node: Alpine + `openssh-server` + `stress-ng`.
- `stress-ng --cpu 1 --cpu-load <random>%` chạy định kỳ để tạo dao động CPU thật, phục vụ demo dashboard/alert sống động.
- Các node này được EMS coi như Device bình thường (`deviceType = GNODEB/AMF/SMF/UPF/UDM`), Collector SSH vào lấy CPU/RAM như mọi Device khác — không cần logic đặc biệt ở backend.

## Seed dữ liệu mẫu
- Seed Device cho 5 node 5G Core (và Device mẫu khác nếu cần demo) khi app khởi động lần đầu — xem cơ chế seed ở `database/jdl.md` (MongoDB không dùng Liquibase, cần cách seed riêng).

## Definition of Done liên quan
- [ ] `docker compose up -d` chạy được toàn bộ stack từ máy sạch.
- [ ] Healthcheck `/management/health` hoạt động, container "healthy" đúng nghĩa.
- [ ] README đủ thông tin để người review tự chạy và thấy dashboard có dữ liệu ngay.

## Liên quan
- `database/jdl.md` — cấu trúc dữ liệu MongoDB và seed.
- `backend/collector.md` — node 5G Core được Collector quét như Device thường.
