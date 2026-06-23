import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { Subscription } from 'rxjs';

import { AccountService } from 'app/core/auth/account.service';
import { WebsocketService } from 'app/core/websocket/websocket.service';
import { DashboardService, DashboardSummary } from './dashboard.service';

@Component({
  selector: 'jhi-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  imports: [RouterLink, FontAwesomeModule],
})
export default class Dashboard implements OnInit, OnDestroy {
  readonly summary = signal<DashboardSummary>({
    totalDevices: 0,
    online: 0,
    offline: 0,
    unknown: 0,
    openAlerts: 0,
    criticalAlerts: 0,
    warningAlerts: 0,
  });

  readonly account = inject(AccountService).account;

  private readonly dashboardService = inject(DashboardService);
  private readonly wsService = inject(WebsocketService);
  private readonly cdr = inject(ChangeDetectorRef);

  private subs: Subscription[] = [];

  ngOnInit(): void {
    this.loadSummary();
    this.wsService.connect();

    this.subs.push(
      this.wsService.deviceStatus$.subscribe(msg => {
        this.summary.update(s => {
          const prev = s;
          const updated = { ...prev };
          updated.online = prev.online + (msg.status === 'ONLINE' ? 1 : 0);
          updated.offline = prev.offline + (msg.status === 'OFFLINE' ? 1 : 0);
          return updated;
        });
        this.loadSummary();
      }),
      this.wsService.alertEvents$.subscribe(() => {
        this.loadSummary();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  private loadSummary(): void {
    this.dashboardService.getSummary().subscribe({
      next: data => {
        this.summary.set(data);
      },
    });
  }
}
