import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IMetricSample, NewMetricSample } from '../metric-sample.model';

export type PartialUpdateMetricSample = Partial<IMetricSample> & Pick<IMetricSample, 'id'>;

type RestOf<T extends IMetricSample | NewMetricSample> = Omit<T, 'collectedAt'> & {
  collectedAt?: string | null;
};

export type RestMetricSample = RestOf<IMetricSample>;

export type NewRestMetricSample = RestOf<NewMetricSample>;

export type PartialUpdateRestMetricSample = RestOf<PartialUpdateMetricSample>;

@Injectable()
export class MetricSamplesService {
  readonly metricSamplesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly metricSamplesResource = httpResource<RestMetricSample[]>(() => {
    const params = this.metricSamplesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of metricSample that have been fetched. It is updated when the metricSamplesResource emits a new value.
   * In case of error while fetching the metricSamples, the signal is set to an empty array.
   */
  readonly metricSamples = computed(() =>
    (this.metricSamplesResource.hasValue() ? this.metricSamplesResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/metric-samples');

  protected convertValueFromServer(restMetricSample: RestMetricSample): IMetricSample {
    return {
      ...restMetricSample,
      collectedAt: restMetricSample.collectedAt ? dayjs(restMetricSample.collectedAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class MetricSampleService extends MetricSamplesService {
  protected readonly http = inject(HttpClient);

  create(metricSample: NewMetricSample): Observable<IMetricSample> {
    const copy = this.convertValueFromClient(metricSample);
    return this.http.post<RestMetricSample>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(metricSample: IMetricSample): Observable<IMetricSample> {
    const copy = this.convertValueFromClient(metricSample);
    return this.http
      .put<RestMetricSample>(`${this.resourceUrl}/${encodeURIComponent(this.getMetricSampleIdentifier(metricSample))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(metricSample: PartialUpdateMetricSample): Observable<IMetricSample> {
    const copy = this.convertValueFromClient(metricSample);
    return this.http
      .patch<RestMetricSample>(`${this.resourceUrl}/${encodeURIComponent(this.getMetricSampleIdentifier(metricSample))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: string): Observable<IMetricSample> {
    return this.http
      .get<RestMetricSample>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IMetricSample[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestMetricSample[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: string): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getMetricSampleIdentifier(metricSample: Pick<IMetricSample, 'id'>): string {
    return metricSample.id;
  }

  compareMetricSample(o1: Pick<IMetricSample, 'id'> | null, o2: Pick<IMetricSample, 'id'> | null): boolean {
    return o1 && o2 ? this.getMetricSampleIdentifier(o1) === this.getMetricSampleIdentifier(o2) : o1 === o2;
  }

  addMetricSampleToCollectionIfMissing<Type extends Pick<IMetricSample, 'id'>>(
    metricSampleCollection: Type[],
    ...metricSamplesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const metricSamples: Type[] = metricSamplesToCheck.filter(isPresent);
    if (metricSamples.length > 0) {
      const metricSampleCollectionIdentifiers = metricSampleCollection.map(metricSampleItem =>
        this.getMetricSampleIdentifier(metricSampleItem),
      );
      const metricSamplesToAdd = metricSamples.filter(metricSampleItem => {
        const metricSampleIdentifier = this.getMetricSampleIdentifier(metricSampleItem);
        if (metricSampleCollectionIdentifiers.includes(metricSampleIdentifier)) {
          return false;
        }
        metricSampleCollectionIdentifiers.push(metricSampleIdentifier);
        return true;
      });
      return [...metricSamplesToAdd, ...metricSampleCollection];
    }
    return metricSampleCollection;
  }

  protected convertValueFromClient<T extends IMetricSample | NewMetricSample | PartialUpdateMetricSample>(metricSample: T): RestOf<T> {
    return {
      ...metricSample,
      collectedAt: metricSample.collectedAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestMetricSample): IMetricSample {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestMetricSample[]): IMetricSample[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
