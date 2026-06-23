import { HttpClient, HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import dayjs from 'dayjs/esm';

import { ITEMS_PER_PAGE, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ItemCount } from 'app/shared/pagination';

interface IAuditLog {
  id: string;
  username?: string;
  action?: string;
  entityName?: string;
  entityId?: string;
  detail?: string;
  timestamp?: dayjs.Dayjs;
}

@Component({
  selector: 'jhi-admin-audit-logs',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './audit-logs.html',
  imports: [RouterLink, FormsModule, FontAwesomeModule, FormatMediumDatetimePipe, NgbPagination, ItemCount],
})
export default class AdminAuditLogs implements OnInit {
  readonly auditLogs = signal<IAuditLog[]>([]);
  readonly totalItems = signal(0);
  readonly isLoading = signal(false);

  page = 1;
  readonly itemsPerPage = ITEMS_PER_PAGE;

  filterUsername = '';
  filterAction = '';
  filterEntityName = '';
  filterFrom = '';
  filterTo = '';

  expandedId: string | null = null;

  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  private get resourceUrl(): string {
    return this.applicationConfigService.getEndpointFor('api/admin/audit-logs');
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    const params: any = {
      page: this.page - 1,
      size: this.itemsPerPage,
      sort: 'timestamp,desc',
    };
    if (this.filterUsername) params['username'] = this.filterUsername;
    if (this.filterAction) params['action'] = this.filterAction;
    if (this.filterEntityName) params['entityName'] = this.filterEntityName;
    if (this.filterFrom) params['from'] = dayjs(this.filterFrom).toISOString();
    if (this.filterTo) params['to'] = dayjs(this.filterTo).toISOString();

    this.http.get<any[]>(this.resourceUrl, { params: createRequestOption(params), observe: 'response' }).subscribe({
      next: (res: HttpResponse<any[]>) => {
        this.totalItems.set(Number(res.headers.get(TOTAL_COUNT_RESPONSE_HEADER)));
        const body: IAuditLog[] = (res.body ?? []).map(item => ({
          ...item,
          timestamp: item.timestamp ? dayjs(item.timestamp) : undefined,
        }));
        this.auditLogs.set(body);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  applyFilter(): void {
    this.page = 1;
    this.load();
  }

  clearFilter(): void {
    this.filterUsername = '';
    this.filterAction = '';
    this.filterEntityName = '';
    this.filterFrom = '';
    this.filterTo = '';
    this.applyFilter();
  }

  onPageChange(p: number): void {
    this.page = p;
    this.load();
  }

  toggleExpand(id: string): void {
    this.expandedId = this.expandedId === id ? null : id;
  }

  formatDetail(detail: string | undefined): string {
    if (!detail) return '';
    try {
      return JSON.stringify(JSON.parse(detail), null, 2);
    } catch {
      return detail;
    }
  }
}
