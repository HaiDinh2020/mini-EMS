# Frontend Domain: Dashboard

## Vai trò
Trang chính sau khi đăng nhập, hiển thị tổng quan trạng thái toàn hệ thống và cập nhật realtime, không cần reload/polling.

## Thành phần UI
- **Card tổng hợp**: số lượng Device theo từng status (Online / Offline / Warning / Critical).
- **Bảng danh sách Device**: tên, IP, type, status (badge màu theo status), `lastCheckedAt`.
- **Biểu đồ CPU/RAM theo thời gian** cho từng Device, dùng `ng2-charts` (Chart.js) — nguồn dữ liệu lấy từ REST API `metric_samples` theo `deviceId` (xem `database/jdl.md`).
- **Alert banner**: hiển thị alert đang `OPEN`/`CRITICAL` nổi bật trên đầu trang.

## Trạng thái UX bắt buộc
Mỗi vùng dữ liệu trên Dashboard (card tổng hợp, bảng Device, biểu đồ, banner alert) phải xử lý đủ 3 trạng thái:
- **Loading**: skeleton/spinner khi đang gọi REST API lần đầu.
- **Empty**: chưa có Device nào / chưa có metric nào (vd hệ thống mới seed, chưa chạy collector vòng nào) — hiển thị thông báo rõ, không để bảng trống trơn không giải thích.
- **Error**: gọi API lỗi (mất kết nối backend, lỗi 5xx) — hiển thị thông báo lỗi, có nút retry, không hiển thị dữ liệu cũ giả như đang còn hợp lệ.

## Realtime qua WebSocket
- Subscribe `/topic/device-status` ngay khi component Dashboard `ngOnInit`, cập nhật status badge của Device tương ứng trong bảng (tìm theo `deviceId`, không reload toàn bảng).
- Subscribe `/topic/alerts` để cập nhật banner alert và (nếu áp dụng) đẩy alert mới vào danh sách không cần fetch lại.
- **Unsubscribe** cả 2 topic trong `ngOnDestroy` để tránh leak subscription khi rời trang.
- Xem cấu trúc payload chi tiết ở `backend/websocket.md`.

## Hiển thị theo role (RBAC ở UI)
- **Admin**: thấy thêm nút/đường dẫn quản lý `AlertRule` và `Credential` ngay trên Dashboard hoặc menu liên quan.
- **User**: chỉ xem Dashboard, Device list, Alert list, Topology — không thấy nút tạo/sửa/xóa.
- Route guard dùng `UserRouteAccessService` có sẵn của JHipster để chặn truy cập trực tiếp URL quản lý nếu không đủ quyền — **không chỉ ẩn UI**, vì API backend cũng chặn ở `@PreAuthorize` (xem `domain/security.md`).

## Responsive
- Dùng Bootstrap có sẵn của JHipster, đảm bảo bảng Device và card tổng hợp scroll/wrap hợp lý trên màn hình nhỏ — đủ để demo trên nhiều kích thước, không cần thiết kế responsive phức tạp.

## Liên quan
- `backend/websocket.md` — cấu trúc topic và payload tiêu thụ tại đây.
- `domain/device.md`, `domain/alert.md` — nguồn dữ liệu hiển thị.
- `domain/security.md` — quy tắc RBAC áp dụng song song ở route guard và API.
