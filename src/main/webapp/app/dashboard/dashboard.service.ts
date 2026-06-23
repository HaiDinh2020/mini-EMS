import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

export interface DashboardSummary {
  totalDevices: number;
  online: number;
  offline: number;
  unknown: number;
  openAlerts: number;
  criticalAlerts: number;
  warningAlerts: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(this.applicationConfigService.getEndpointFor('api/dashboard/summary'));
  }
}
