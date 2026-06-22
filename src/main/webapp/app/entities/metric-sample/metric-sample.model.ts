import dayjs from 'dayjs/esm';

import { IDevice } from 'app/entities/device/device.model';

export interface IMetricSample {
  id: string;
  cpuUsage?: number | null;
  ramUsage?: number | null;
  diskUsage?: number | null;
  pingLatencyMs?: number | null;
  collectedAt?: dayjs.Dayjs | null;
  device?: Pick<IDevice, 'id' | 'name'> | null;
}

export type NewMetricSample = Omit<IMetricSample, 'id'> & { id: null };
