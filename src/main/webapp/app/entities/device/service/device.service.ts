import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IDevice, NewDevice } from '../device.model';

export type PartialUpdateDevice = Partial<IDevice> & Pick<IDevice, 'id'>;

type RestOf<T extends IDevice | NewDevice> = Omit<T, 'lastCheckedAt'> & {
  lastCheckedAt?: string | null;
};

export type RestDevice = RestOf<IDevice>;

export type NewRestDevice = RestOf<NewDevice>;

export type PartialUpdateRestDevice = RestOf<PartialUpdateDevice>;

@Injectable()
export class DevicesService {
  readonly devicesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly devicesResource = httpResource<RestDevice[]>(() => {
    const params = this.devicesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of device that have been fetched. It is updated when the devicesResource emits a new value.
   * In case of error while fetching the devices, the signal is set to an empty array.
   */
  readonly devices = computed(() =>
    (this.devicesResource.hasValue() ? this.devicesResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/devices');

  protected convertValueFromServer(restDevice: RestDevice): IDevice {
    return {
      ...restDevice,
      lastCheckedAt: restDevice.lastCheckedAt ? dayjs(restDevice.lastCheckedAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class DeviceService extends DevicesService {
  protected readonly http = inject(HttpClient);

  create(device: NewDevice): Observable<IDevice> {
    const copy = this.convertValueFromClient(device);
    return this.http.post<RestDevice>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(device: IDevice): Observable<IDevice> {
    const copy = this.convertValueFromClient(device);
    return this.http
      .put<RestDevice>(`${this.resourceUrl}/${encodeURIComponent(this.getDeviceIdentifier(device))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(device: PartialUpdateDevice): Observable<IDevice> {
    const copy = this.convertValueFromClient(device);
    return this.http
      .patch<RestDevice>(`${this.resourceUrl}/${encodeURIComponent(this.getDeviceIdentifier(device))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: string): Observable<IDevice> {
    return this.http.get<RestDevice>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IDevice[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestDevice[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: string): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getDeviceIdentifier(device: Pick<IDevice, 'id'>): string {
    return device.id;
  }

  compareDevice(o1: Pick<IDevice, 'id'> | null, o2: Pick<IDevice, 'id'> | null): boolean {
    return o1 && o2 ? this.getDeviceIdentifier(o1) === this.getDeviceIdentifier(o2) : o1 === o2;
  }

  addDeviceToCollectionIfMissing<Type extends Pick<IDevice, 'id'>>(
    deviceCollection: Type[],
    ...devicesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const devices: Type[] = devicesToCheck.filter(isPresent);
    if (devices.length > 0) {
      const deviceCollectionIdentifiers = deviceCollection.map(deviceItem => this.getDeviceIdentifier(deviceItem));
      const devicesToAdd = devices.filter(deviceItem => {
        const deviceIdentifier = this.getDeviceIdentifier(deviceItem);
        if (deviceCollectionIdentifiers.includes(deviceIdentifier)) {
          return false;
        }
        deviceCollectionIdentifiers.push(deviceIdentifier);
        return true;
      });
      return [...devicesToAdd, ...deviceCollection];
    }
    return deviceCollection;
  }

  protected convertValueFromClient<T extends IDevice | NewDevice | PartialUpdateDevice>(device: T): RestOf<T> {
    return {
      ...device,
      lastCheckedAt: device.lastCheckedAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestDevice): IDevice {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestDevice[]): IDevice[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
