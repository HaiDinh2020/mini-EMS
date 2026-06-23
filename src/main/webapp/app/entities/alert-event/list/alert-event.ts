import { HttpHeaders } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';
import { Subscription, combineLatest, filter, tap } from 'rxjs';

import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { AccountService } from 'app/core/auth/account.service';
import { WebsocketService } from 'app/core/websocket/websocket.service';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { ItemCount } from 'app/shared/pagination';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { IAlertEvent } from '../alert-event.model';
import { AlertEventDeleteDialog } from '../delete/alert-event-delete-dialog';
import { AlertEventService } from '../service/alert-event.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-alert-event',
  templateUrl: './alert-event.html',
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    AlertError,
    Alert,
    SortDirective,
    SortByDirective,
    TranslateDirective,
    TranslateModule,
    FormatMediumDatetimePipe,
    NgbPagination,
    ItemCount,
    HasAnyAuthorityDirective,
  ],
})
export class AlertEvent implements OnInit, OnDestroy {
  subscription: Subscription | null = null;
  wsSub: Subscription | null = null;
  readonly alertEvents = signal<IAlertEvent[]>([]);

  sortState = sortStateSignal({});

  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly totalItems = signal(0);
  readonly page = signal(1);

  // filter state
  filterStatus = '';
  filterSeverity = '';

  readonly router = inject(Router);
  protected readonly accountService = inject(AccountService);
  protected readonly alertEventService = inject(AlertEventService);
  private readonly wsService = inject(WebsocketService);
  // eslint-disable-next-line @typescript-eslint/member-ordering
  readonly isLoading = this.alertEventService.alertEventsResource.isLoading;
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);

  constructor() {
    effect(() => {
      const headers = this.alertEventService.alertEventsResource.headers();
      if (headers) {
        this.fillComponentAttributesFromResponseHeader(headers);
      }
    });
    effect(() => {
      this.alertEvents.set(this.fillComponentAttributesFromResponseBody([...this.alertEventService.alertEvents()]));
    });
  }

  trackId = (item: IAlertEvent): string => this.alertEventService.getAlertEventIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();

    // Subscribe to WebSocket for realtime row updates
    this.wsService.connect();
    this.wsSub = this.wsService.alertEvents$.subscribe(msg => {
      this.alertEvents.update(events =>
        events.map(ev => {
          if (ev.id === msg.alertEventId) {
            return { ...ev, status: msg.status as any, severity: msg.severity as any };
          }
          return ev;
        }),
      );
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.wsSub?.unsubscribe();
  }

  acknowledge(alertEvent: IAlertEvent): void {
    this.alertEventService.acknowledge(alertEvent.id).subscribe(updated => {
      this.alertEvents.update(events => events.map(ev => (ev.id === updated.id ? updated : ev)));
    });
  }

  applyFilter(): void {
    this.page.set(1);
    this.load();
  }

  clearFilter(): void {
    this.filterStatus = '';
    this.filterSeverity = '';
    this.applyFilter();
  }

  delete(alertEvent: IAlertEvent): void {
    const modalRef = this.modalService.open(AlertEventDeleteDialog, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.alertEvent = alertEvent;
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend();
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page(), event);
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState());
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page.set(+(page ?? 1));
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
    this.filterStatus = params.get('status') ?? '';
    this.filterSeverity = params.get('severity') ?? '';
  }

  protected fillComponentAttributesFromResponseBody(data: IAlertEvent[]): IAlertEvent[] {
    return data;
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems.set(Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER)));
  }

  protected queryBackend(): void {
    const pageToLoad: number = this.page();
    const queryObject: any = {
      page: pageToLoad - 1,
      size: this.itemsPerPage(),
      eagerload: true,
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    if (this.filterStatus) {
      queryObject['status.equals'] = this.filterStatus;
    }
    if (this.filterSeverity) {
      queryObject['severity.equals'] = this.filterSeverity;
    }
    this.alertEventService.alertEventsParams.set(queryObject);
  }

  protected handleNavigation(page: number, sortState: SortState): void {
    const queryParamsObj: any = {
      page,
      size: this.itemsPerPage(),
      sort: this.sortService.buildSortParam(sortState),
    };
    if (this.filterStatus) {
      queryParamsObj['status'] = this.filterStatus;
    }
    if (this.filterSeverity) {
      queryParamsObj['severity'] = this.filterSeverity;
    }

    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: queryParamsObj,
    });
  }
}
