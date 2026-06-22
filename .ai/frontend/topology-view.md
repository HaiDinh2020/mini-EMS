# Frontend Domain: Topology View (Bonus)

## Vai trò
Hiển thị trực quan mạng các Device dưới dạng graph (node-link), giúp xem nhanh trạng thái và quan hệ kết nối giữa các thiết bị — đặc biệt phù hợp để minh họa mạng 5G Core giả lập (gNodeB/AMF/SMF/UPF/UDM).

## Công nghệ
- Dùng **`vis-network`** hoặc **`d3-force`** trong 1 Angular component riêng (`topology-view.component`), tách biệt khỏi component Dashboard.
- Không bắt buộc realtime ở mức graph layout (vẽ lại toàn graph mỗi lần có update là không cần thiết) — chỉ cập nhật màu node theo status khi có message từ `/topic/device-status`.

## Dữ liệu nguồn
- **Node** = `Device` (lấy từ REST API Device list).
- **Edge** = `TopologyLink` (lấy từ REST API TopologyLink, field `sourceDeviceId`/`targetDeviceId` — xem `database/jdl.md`).
- Map `Device.status` → màu node:
  - `ONLINE` → xanh
  - `WARNING` → vàng/cam
  - `CRITICAL` → đỏ
  - `OFFLINE`/`UNKNOWN` → xám
- Map `TopologyLink.status` → kiểu/màu đường nối:
  - `UP` → đường liền, màu thường
  - `DEGRADED` → đường nét đứt hoặc màu cảnh báo
  - `DOWN` → đường đỏ hoặc ẩn nhãn "down"

## Tương tác
- Click vào node → mở panel chi tiết (sidebar hoặc modal) hiển thị:
  - Thông tin cơ bản Device (name, IP, type, location).
  - Metric mới nhất (CPU/RAM/Disk) — gọi REST API `metric_samples` lấy bản ghi gần nhất theo `deviceId`.
  - Danh sách `AlertEvent` đang `OPEN` của Device đó.
- Click vào edge (tuỳ chọn, không bắt buộc) → hiển thị tooltip `linkType`, `bandwidthMbps`, `status`.

## Trạng thái UX
- **Loading**: hiển thị spinner khi đang fetch Device + TopologyLink lần đầu.
- **Empty**: nếu chưa có `TopologyLink` nào được tạo, hiển thị thông báo rõ ("Chưa có liên kết topology nào") kèm gợi ý cho Admin tạo link, không để canvas trống không giải thích.
- **Error**: lỗi gọi API → thông báo lỗi + nút retry, không vẽ graph với dữ liệu rỗng giả.

## Quyền truy cập
- Cả `ROLE_USER` và `ROLE_ADMIN` đều xem được trang này (read-only theo định hướng RBAC ở `domain/security.md`).
- Việc tạo/sửa/xóa `TopologyLink` (nếu có UI riêng cho việc này) chỉ `ROLE_ADMIN` được phép, theo cùng nguyên tắc với Device/Credential/AlertRule.

## Liên quan
- `domain/topology.md` — schema và nghiệp vụ `TopologyLink`.
- `domain/device.md` — nguồn dữ liệu node và mapping status.
- `backend/websocket.md` — cập nhật màu node realtime qua `/topic/device-status`.
