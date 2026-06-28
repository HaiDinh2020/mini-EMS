import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

export interface TopologyNode {
  id: string;
  name: string;
  deviceType: string;
  status: string;
  ipAddress: string;
}

export interface TopologyEdge {
  id: string;
  linkType: string | null;
  bandwidthMbps: number | null;
  status: string | null;
  sourceId: string;
  sourceName: string;
  targetId: string;
  targetName: string;
}

@Injectable({ providedIn: 'root' })
export class TopologyService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApplicationConfigService);

  getDevices(): Observable<TopologyNode[]> {
    return this.http.get<TopologyNode[]>(this.config.getEndpointFor('api/topology/devices'));
  }

  getLinks(): Observable<TopologyEdge[]> {
    return this.http.get<TopologyEdge[]>(this.config.getEndpointFor('api/topology/links'));
  }
}
