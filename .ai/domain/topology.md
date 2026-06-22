# Domain: Topology

## Trách nhiệm
Mô hình hóa kết nối vật lý/logic giữa các thiết bị. Dữ liệu này được dùng để vẽ topology map trên frontend.

## MongoDB Document

```java
@Document(collection = "topology_links")
public class TopologyLink {
    @Id
    private String id;

    @Indexed
    private String sourceDeviceId;

    @Indexed
    private String targetDeviceId;

    private String linkType;        // "physical", "logical", "5G-N2", "5G-N3"...
    private Integer bandwidthMbps;
    private LinkStatus status;      // UP, DOWN, DEGRADED
}
```

```java
enum LinkStatus { UP, DOWN, DEGRADED }
```

## DTO

```java
public class TopologyLinkDTO {
    private String id;
    private String sourceDeviceId;
    private String sourceDeviceName;    // enriched khi query
    private DeviceStatus sourceStatus;  // enriched khi query
    private String targetDeviceId;
    private String targetDeviceName;
    private DeviceStatus targetStatus;
    private String linkType;
    private Integer bandwidthMbps;
    private LinkStatus status;
}
```

## REST API

| Method | Path | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/topology` | USER | Toàn bộ graph: nodes (devices) + edges (links) |
| POST | `/api/topology/links` | ADMIN | Tạo link mới |
| PUT | `/api/topology/links/{id}` | ADMIN | Cập nhật link |
| DELETE | `/api/topology/links/{id}` | ADMIN | Xóa link |

## Topology Graph Response

`GET /api/topology` trả về 1 object gồm cả nodes và edges để frontend render 1 lần:

```json
{
  "nodes": [
    {
      "id": "dev001",
      "label": "node-gnodeb",
      "deviceType": "GNODEB",
      "status": "ONLINE",
      "ipAddress": "172.20.0.10",
      "latestMetrics": { "cpuUsage": 45.2, "ramUsage": 62.1 }
    }
  ],
  "edges": [
    {
      "id": "link001",
      "from": "dev001",
      "to": "dev002",
      "linkType": "5G-N2",
      "status": "UP",
      "bandwidthMbps": 1000
    }
  ]
}
```

## Query Patterns

```java
// Lấy tất cả link có liên quan đến 1 device
List<TopologyLink> findBySourceDeviceIdOrTargetDeviceId(String id1, String id2);

// Lấy toàn bộ link
List<TopologyLink> findAll();
```

## Seed Data mẫu (5G Core topology)

```
gNodeB ──N2──► AMF
gNodeB ──N3──► UPF
AMF ──────────► SMF
AMF ──────────► UDM
SMF ──────────► UPF
```

Seed qua `ApplicationRunner` hoặc mongoimport khi khởi động lần đầu.

## Status tự động cập nhật
- Khi `sourceDevice` hoặc `targetDevice` chuyển sang OFFLINE → link status → `DOWN`
- Logic này nằm trong `DeviceCollectorService` sau khi cập nhật Device.status
- Broadcast status thay đổi qua `/topic/device-status` để frontend cập nhật topology realtime

## Liên kết
- Topology view frontend: `frontend/topology-view.md`
- Device status broadcast: `backend/websocket.md`
