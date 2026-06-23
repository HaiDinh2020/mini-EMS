import { Injectable, OnDestroy, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { AlertEventMessage, WebsocketService } from 'app/core/websocket/websocket.service';
import { DashboardService } from 'app/dashboard/dashboard.service';

export interface AlertToast {
  id: string;
  alertEventId: string;
  deviceName: string;
  metricType: string;
  severity: string;
  value: number;
  message: string;
  timestamp: string;
}

const MAX_TOASTS = 5;

@Injectable({ providedIn: 'root' })
export class AlertNotificationService implements OnDestroy {
  readonly openAlertCount = signal(0);
  readonly toasts = signal<AlertToast[]>([]);

  readonly hasCritical = computed(() => {
    return this.toasts().some(t => t.severity === 'CRITICAL');
  });

  private readonly wsService = inject(WebsocketService);
  private readonly dashboardService = inject(DashboardService);
  private readonly router = inject(Router);

  private sub: Subscription | null = null;

  init(): void {
    this.dashboardService.getSummary().subscribe({
      next: summary => this.openAlertCount.set(summary.openAlerts),
    });

    this.wsService.connect();
    this.sub = this.wsService.alertEvents$.subscribe(msg => this.handleAlert(msg));
  }

  dismiss(alertEventId: string): void {
    this.toasts.update(ts => ts.filter(t => t.alertEventId !== alertEventId));
  }

  navigateToAlerts(): void {
    this.router.navigate(['/alert-event']);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  private handleAlert(msg: AlertEventMessage): void {
    if (msg.status === 'OPEN') {
      this.openAlertCount.update(c => c + 1);

      const toast: AlertToast = {
        id: `toast-${Date.now()}`,
        alertEventId: msg.alertEventId,
        deviceName: msg.deviceName,
        metricType: msg.metricType,
        severity: msg.severity,
        value: msg.value,
        message: msg.message,
        timestamp: msg.timestamp,
      };

      this.toasts.update(ts => {
        const updated = [toast, ...ts.filter(t => t.alertEventId !== msg.alertEventId)];
        return updated.slice(0, MAX_TOASTS);
      });
    } else if (msg.status === 'RESOLVED') {
      this.openAlertCount.update(c => Math.max(0, c - 1));
      this.toasts.update(ts => ts.filter(t => t.alertEventId !== msg.alertEventId));
    }
  }
}
