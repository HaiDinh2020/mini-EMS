import dayjs from 'dayjs/esm';

import { DeviceStatus } from 'app/entities/enumerations/device-status.model';
import { DeviceType } from 'app/entities/enumerations/device-type.model';

export interface IDevice {
  id: string;
  name?: string | null;
  ipAddress?: string | null;
  hostname?: string | null;
  deviceType?: keyof typeof DeviceType | null;
  vendor?: string | null;
  model?: string | null;
  sshPort?: number | null;
  sshUsername?: string | null;
  location?: string | null;
  status?: keyof typeof DeviceStatus | null;
  lastCheckedAt?: dayjs.Dayjs | null;
  monitoringEnabled?: boolean | null;
  description?: string | null;
  credentialId?: string | null;
}

export type NewDevice = Omit<IDevice, 'id'> & { id: null };
