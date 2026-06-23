import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IAlertEvent, NewAlertEvent } from '../alert-event.model';

export type PartialUpdateAlertEvent = Partial<IAlertEvent> & Pick<IAlertEvent, 'id'>;

type RestOf<T extends IAlertEvent | NewAlertEvent> = Omit<T, 'triggeredAt' | 'resolvedAt'> & {
  triggeredAt?: string | null;
  resolvedAt?: string | null;
};

export type RestAlertEvent = RestOf<IAlertEvent>;

export type NewRestAlertEvent = RestOf<NewAlertEvent>;

export type PartialUpdateRestAlertEvent = RestOf<PartialUpdateAlertEvent>;

@Injectable()
export class AlertEventsService {
  readonly alertEventsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly alertEventsResource = httpResource<RestAlertEvent[]>(() => {
    const params = this.alertEventsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of alertEvent that have been fetched. It is updated when the alertEventsResource emits a new value.
   * In case of error while fetching the alertEvents, the signal is set to an empty array.
   */
  readonly alertEvents = computed(() =>
    (this.alertEventsResource.hasValue() ? this.alertEventsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/alert-events');

  protected convertValueFromServer(restAlertEvent: RestAlertEvent): IAlertEvent {
    return {
      ...restAlertEvent,
      triggeredAt: restAlertEvent.triggeredAt ? dayjs(restAlertEvent.triggeredAt) : undefined,
      resolvedAt: restAlertEvent.resolvedAt ? dayjs(restAlertEvent.resolvedAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class AlertEventService extends AlertEventsService {
  protected readonly http = inject(HttpClient);

  create(alertEvent: NewAlertEvent): Observable<IAlertEvent> {
    const copy = this.convertValueFromClient(alertEvent);
    return this.http.post<RestAlertEvent>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(alertEvent: IAlertEvent): Observable<IAlertEvent> {
    const copy = this.convertValueFromClient(alertEvent);
    return this.http
      .put<RestAlertEvent>(`${this.resourceUrl}/${encodeURIComponent(this.getAlertEventIdentifier(alertEvent))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(alertEvent: PartialUpdateAlertEvent): Observable<IAlertEvent> {
    const copy = this.convertValueFromClient(alertEvent);
    return this.http
      .patch<RestAlertEvent>(`${this.resourceUrl}/${encodeURIComponent(this.getAlertEventIdentifier(alertEvent))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: string): Observable<IAlertEvent> {
    return this.http
      .get<RestAlertEvent>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IAlertEvent[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestAlertEvent[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  acknowledge(id: string): Observable<IAlertEvent> {
    return this.http
      .put<RestAlertEvent>(`${this.resourceUrl}/${encodeURIComponent(id)}/acknowledge`, {})
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  delete(id: string): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getAlertEventIdentifier(alertEvent: Pick<IAlertEvent, 'id'>): string {
    return alertEvent.id;
  }

  compareAlertEvent(o1: Pick<IAlertEvent, 'id'> | null, o2: Pick<IAlertEvent, 'id'> | null): boolean {
    return o1 && o2 ? this.getAlertEventIdentifier(o1) === this.getAlertEventIdentifier(o2) : o1 === o2;
  }

  addAlertEventToCollectionIfMissing<Type extends Pick<IAlertEvent, 'id'>>(
    alertEventCollection: Type[],
    ...alertEventsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const alertEvents: Type[] = alertEventsToCheck.filter(isPresent);
    if (alertEvents.length > 0) {
      const alertEventCollectionIdentifiers = alertEventCollection.map(alertEventItem => this.getAlertEventIdentifier(alertEventItem));
      const alertEventsToAdd = alertEvents.filter(alertEventItem => {
        const alertEventIdentifier = this.getAlertEventIdentifier(alertEventItem);
        if (alertEventCollectionIdentifiers.includes(alertEventIdentifier)) {
          return false;
        }
        alertEventCollectionIdentifiers.push(alertEventIdentifier);
        return true;
      });
      return [...alertEventsToAdd, ...alertEventCollection];
    }
    return alertEventCollection;
  }

  protected convertValueFromClient<T extends IAlertEvent | NewAlertEvent | PartialUpdateAlertEvent>(alertEvent: T): RestOf<T> {
    return {
      ...alertEvent,
      triggeredAt: alertEvent.triggeredAt?.toJSON() ?? null,
      resolvedAt: alertEvent.resolvedAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestAlertEvent): IAlertEvent {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestAlertEvent[]): IAlertEvent[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
