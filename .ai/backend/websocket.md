# Backend Domain: WebSocket Realtime

## Vai trò
Đẩy cập nhật trạng thái Device và Alert tới Dashboard Angular theo thời gian thực, thay cho việc client polling liên tục.

## Nền tảng
- Dùng module **WebSocket STOMP** có sẵn của JHipster (`websocket-configuration.ts` phía Angular, `WebsocketSecurityConfiguration` phía Spring Boot) — không tự viết lại hạ tầng STOMP, chỉ thêm topic nghiệp vụ.
- Broker nội bộ (simple broker), không cần RabbitMQ/Kafka cho phạm vi bài toán này.

## Topic nghiệp vụ
| Topic | Khi nào publish | Payload |
|---|---|---|
| `/topic/device-status` | Sau mỗi vòng Collector cập nhật `Device.status`/`lastCheckedAt` | `{ deviceId, name, status, lastCheckedAt }` |
| `/topic/alerts` | Mỗi lần `AlertEvent` được tạo mới hoặc đổi trạng thái (OPEN/ACKNOWLEDGED/RESOLVED) | `{ alertId, deviceId, metricType, severity, status, value, triggeredAt }` |

## Quy ước payload
- Payload là JSON, field name theo camelCase, khớp với DTO trả ra ở REST API tương ứng (không tạo 2 format khác nhau cho cùng 1 entity).
- Không gửi field nhạy cảm (không có trường nào liên quan Credential/secret trong các topic trên).
- Vì 2 topic này **không nằm trong OpenAPI/Swagger tự sinh**, phải tài liệu hoá riêng cấu trúc payload (trong README hoặc file API doc bổ sung).

## Nơi publish trong code
- `DeviceCollectorService` → publish `/topic/device-status` sau khi cập nhật status (xem `backend/collector.md`).
- `AlertEvaluatorService` → publish `/topic/alerts` sau khi tạo/đổi trạng thái `AlertEvent` (xem `domain/alert.md`).
- Dùng `SimpMessagingTemplate.convertAndSend(topic, payload)`, gọi từ service layer, **không gọi từ Controller**.

## Bảo mật
- Kết nối WebSocket vẫn yêu cầu JWT hợp lệ (qua `WebsocketSecurityConfiguration` có sẵn của JHipster).
- Không tạo topic riêng cho dữ liệu chỉ Admin được xem (Credential, Audit Log) — các trang đó dùng REST API thông thường + `@PreAuthorize`, không qua WebSocket.

## Frontend tiêu thụ
- Angular subscribe cả 2 topic ngay khi vào trang Dashboard, unsubscribe khi rời trang (tránh leak subscription).
- Xem `frontend/dashboard.md` để biết cách cập nhật UI khi nhận message.

## Liên quan
- `domain/device.md`, `domain/alert.md` — entity nguồn dữ liệu publish.
- `frontend/dashboard.md` — nơi tiêu thụ topic để cập nhật realtime.
