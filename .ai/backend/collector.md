# Backend Domain: Collector Service

## Vai trò
Thu thập dữ liệu giám sát (CPU/RAM/Disk/Uptime/Reachability) từ các Device đã đăng ký, định kỳ theo lịch, ghi kết quả vào `MetricSample` và cập nhật `Device.status`.

## Vị trí trong layer
`service/collector/DeviceCollectorService` — gọi từ Spring `@Scheduled`, không chứa logic REST, không bị gọi trực tiếp từ Controller.

## Cơ chế chạy
- `@Scheduled(fixedDelayString = "${ems.collector.interval-ms}")`, đọc interval từ `application.yml` (không hardcode).
- Chạy trên thread pool riêng (`TaskExecutor` custom, không dùng thread pool mặc định của Spring) để 1 SSH timeout không chặn các Device khác.
- Lấy danh sách Device có `monitoringEnabled = true` từ MongoDB trước mỗi vòng quét.

## Reachability check
- Không dùng ICMP ping thật (tránh phải cấp `CAP_NET_RAW`/chạy container privileged).
- Mở **TCP socket connect** tới `Device.ipAddress : Device.sshPort` (mặc định 22), timeout ngắn (vd 2–3s).
- Reachable → tiếp tục bước SSH thu thập metric.
- Không reachable → set `Device.status = OFFLINE`, vẫn ghi `MetricSample` với giá trị null/marker để giữ liên tục time-series (tuỳ chọn, xem `database/indexing.md`).

## SSH collector
- Dùng thư viện **sshj**.
- Lấy `Credential` gắn với Device, decrypt secret (Jasypt) **chỉ trong RAM** tại thời điểm gọi SSH, không log, không cache plaintext.
- Lệnh chạy trên target Linux node:
  - CPU: `top -bn1 | grep "Cpu(s)"` hoặc đọc `/proc/stat` 2 lần cách nhau 1s để tính % chính xác hơn.
  - RAM: `free -m`, parse `used/total`.
  - Disk: `df -h /`.
  - Uptime: `cat /proc/uptime`.

## Parser — pure function, unit test được
- Mỗi lệnh có 1 hàm parser thuần (input: text output cố định, output: số liệu đã parse), **không phụ thuộc I/O**.
- Ví dụ: `parseFreeOutput(String raw): RamStats`, `parseTopCpuLine(String raw): Double`.
- Unit test bằng input mẫu cố định của `free -m`, `top -bn1` (xem `coding-rules.md` mục testing).

## Ghi kết quả
- Tạo document `MetricSample` mới (collection riêng, time-series — xem `domain/metric.md`).
- Cập nhật `Device.status`, `Device.lastCheckedAt` (update field trong document Device, không tạo bản ghi mới).
- Sau khi ghi `MetricSample` → gọi `AlertEvaluatorService` (xem `domain/alert.md`) để so khớp ngưỡng.

## Lỗi & resilience
- SSH timeout/connection refused → log ở mức WARN (không log credential), set `Device.status = UNKNOWN` nếu không phân biệt được lý do.
- Một Device lỗi không được làm crash toàn vòng quét — bọc try-catch theo từng Device trong loop, không bọc cả batch.
- Không retry vô hạn trong cùng 1 vòng quét; để vòng `@Scheduled` tiếp theo tự retry.

## Liên quan
- `domain/device.md` — schema Device, trạng thái status.
- `domain/metric.md` — schema MetricSample.
- `domain/alert.md` — alert engine được gọi sau khi có metric mới.
- `database/indexing.md` — index cho truy vấn theo `collectedAt`.
