# Database Domain: Indexing & Data Lifecycle (MongoDB)

## Nguyên tắc
Vì MongoDB không tự động tối ưu truy vấn theo cách RDBMS làm với FK, **mọi field dùng để filter/sort thường xuyên phải có index rõ ràng**, định nghĩa qua Spring Data MongoDB annotation (`@Indexed`) hoặc tạo index thủ công lúc khởi tạo collection.

## Index cần tạo

| Collection | Field | Loại index | Lý do |
|---|---|---|---|
| `devices` | `ipAddress` | Unique index | Tránh trùng IP, tăng tốc tra cứu theo IP (collector, validate khi tạo Device) |
| `devices` | `status` | Single index | Dashboard query count Online/Offline/Warning/Critical thường xuyên |
| `devices` | `monitoringEnabled` | Single index | Collector chỉ quét Device có `monitoringEnabled = true`, query mỗi vòng `@Scheduled` |
| `metric_samples` | `collectedAt` | Single index (hoặc time index nếu dùng time-series collection) | Truy vấn theo khoảng thời gian cho biểu đồ, archive job |
| `metric_samples` | `deviceId + collectedAt` | Compound index | Truy vấn "lịch sử metric của 1 Device theo thời gian" — pattern phổ biến nhất cho biểu đồ Dashboard |
| `alert_events` | `status` | Single index | Lọc alert đang `OPEN` cho banner/trang Alert |
| `alert_events` | `deviceId + status` | Compound index | Truy vấn "alert đang mở của 1 Device" khi xem chi tiết Device/Topology |
| `audit_logs` | `username`, `timestamp` | Compound index | Trang Audit Log filter theo user + thời gian |
| `topology_links` | `sourceDeviceId`, `targetDeviceId` | Single index mỗi field | Dựng graph topology nhanh khi load trang Topology View |

## Time-series & archive cho `metric_samples`
- Dữ liệu `MetricSample` tăng nhanh theo thời gian (mỗi Device, mỗi vòng `@Scheduled`, 1 document mới).
- Ưu tiên dùng **MongoDB Time Series Collection** (xem `database/jdl.md`) để MongoDB tự tối ưu lưu trữ/nén theo `collectedAt`.
- Có job dọn dữ liệu cũ định kỳ (vd giữ raw data 30 ngày, sau đó archive hoặc xoá), tránh collection phình vô hạn:
  - Cách đơn giản: TTL index (`expireAfterSeconds`) trên `collectedAt` nếu chấp nhận mất dữ liệu cũ hoàn toàn.
  - Cách archive: scheduled job export dữ liệu cũ ra file/collection khác trước khi xoá.
- Nêu rõ hướng mở rộng nếu cần scale thật: cân nhắc time-series DB chuyên dụng (TimescaleDB là gợi ý gốc cho SQL; với MongoDB, time-series collection + sharding theo `deviceId` là hướng tương đương).

## Backup
- `mongodump` định kỳ ra volume riêng (tương đương `pg_dump` trong kế hoạch gốc), kèm cron job hoặc container backup riêng trong compose.
- Backup tối thiểu cho các collection cấu hình quan trọng: `devices`, `credentials`, `alert_rules` — đây là dữ liệu không tái tạo được dễ dàng nếu mất.
- `metric_samples`/`audit_logs` có thể backup tần suất thấp hơn (dữ liệu lịch sử, mất một phần ít ảnh hưởng vận hành).

## Validate dữ liệu (thay cho NOT NULL/CHECK constraint của SQL)
- Dùng Bean Validation (`@NotNull`, `@Pattern` cho IP/hostname) ở tầng DTO/Entity Spring Data — đây là lớp validate chính.
- Tuỳ chọn thêm `$jsonSchema` validator ở tầng MongoDB cho các field bắt buộc (`name`, `ipAddress`, `collectedAt`) để có lớp bảo vệ thứ hai khi có script/migration ghi trực tiếp vào DB ngoài luồng Spring Boot.

## Liên quan
- `database/jdl.md` — schema từng collection.
- `backend/collector.md` — nguồn ghi `metric_samples` liên tục.
- `domain/audit.md` — nguồn ghi `audit_logs`.
