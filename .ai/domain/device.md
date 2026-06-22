# Domain: Device

## Trách nhiệm
Quản lý thông tin thiết bị mạng (server, router, 5G Core node…). Đây là entity trung tâm của toàn hệ thống — mọi MetricSample, AlertEvent, TopologyLink đều liên kết về Device.

## MongoDB Document

```java
@Document(collection = "devices")
public class Device {
    @Id
    private String id;

    @NotNull
    private String name;

    @NotNull
    @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")
    private String ipAddress;

    private String hostname;

    @NotNull
    private DeviceType deviceType;

    private String vendor;
    private String model;
    private Integer sshPort;        // default 22
    private String sshUsername;
    private String location;

    private DeviceStatus status;    // cập nhật bởi Collector
    private Instant lastCheckedAt;  // cập nhật bởi Collector
    private Boolean monitoringEnabled;
    private String description;

    @DBRef
    private Credential credential;  // reference đến Credential document
}
```

## Enums

```java
enum DeviceType {
    SERVER, ROUTER, SWITCH, FIREWALL,
    GNODEB, AMF, SMF, UPF, UDM, OTHER
}

enum DeviceStatus {
    ONLINE,     // TCP-connect OK + SSH OK
    OFFLINE,    // TCP-connect thất bại
    UNKNOWN,    // chưa check lần nào
    WARNING,    // đang có AlertEvent WARNING mở
    CRITICAL    // đang có AlertEvent CRITICAL mở
}
```

## DTO

```java
public class DeviceDTO {
    private String id;
    private String name;
    private String ipAddress;
    private String hostname;
    private DeviceType deviceType;
    private String vendor;
    private String model;
    private Integer sshPort;
    private String sshUsername;
    private String location;
    private DeviceStatus status;
    private Instant lastCheckedAt;
    private Boolean monitoringEnabled;
    private String description;
    private String credentialId;    // chỉ trả ID, không trả secret
}
```

## REST API

| Method | Path | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/devices` | USER | Danh sách, hỗ trợ pagination + filter theo status/type |
| POST | `/api/devices` | ADMIN | Tạo device mới |
| GET | `/api/devices/{id}` | USER | Chi tiết 1 device |
| PUT | `/api/devices/{id}` | ADMIN | Cập nhật thông tin |
| DELETE | `/api/devices/{id}` | ADMIN | Xóa device |
| GET | `/api/devices/{id}/metrics` | USER | MetricSample gần nhất của device |
| GET | `/api/devices/{id}/alerts` | USER | AlertEvent đang mở của device |

## Query patterns

```java
// Lấy tất cả device đang monitoring
List<Device> findByMonitoringEnabledTrue();

// Lấy theo status (cho dashboard summary count)
long countByStatus(DeviceStatus status);

// Tìm kiếm theo type (5G core nodes)
List<Device> findByDeviceTypeIn(List<DeviceType> types);
```

## Business Rules
1. Khi tạo Device, `status` mặc định là `UNKNOWN`, `monitoringEnabled` mặc định `true`
2. `sshPort` mặc định `22` nếu không truyền
3. Xóa Device phải cascade xóa MetricSample + AlertEvent liên quan (hoặc soft delete)
4. Không trả `credential` object đầy đủ qua API — chỉ trả `credentialId`
5. `ipAddress` phải unique trong collection

## RBAC
- `ROLE_ADMIN`: full CRUD
- `ROLE_USER`: GET only (list + detail + metrics + alerts)

## Liên kết với domain khác
- `MetricSample` → `deviceId` (xem `domain/metric.md`)
- `AlertEvent` → `deviceId` (xem `domain/alert.md`)
- `TopologyLink` → `sourceDeviceId`, `targetDeviceId` (xem `domain/topology.md`)
- `AuditLog` ghi nhận mọi thao tác CRUD Device (xem `domain/audit.md`)
