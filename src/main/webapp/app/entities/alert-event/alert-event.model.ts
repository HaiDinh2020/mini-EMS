import dayjs from 'dayjs/esm';

import { IAlertRule } from 'app/entities/alert-rule/alert-rule.model';
import { IDevice } from 'app/entities/device/device.model';
import { AlertStatus } from 'app/entities/enumerations/alert-status.model';
import { MetricType } from 'app/entities/enumerations/metric-type.model';
import { Severity } from 'app/entities/enumerations/severity.model';

export interface IAlertEvent {
  id: string;
  metricType?: keyof typeof MetricType | null;
  value?: number | null;
  severity?: keyof typeof Severity | null;
  message?: string | null;
  triggeredAt?: dayjs.Dayjs | null;
  resolvedAt?: dayjs.Dayjs | null;
  status?: keyof typeof AlertStatus | null;
  device?: Pick<IDevice, 'id' | 'name'> | null;
  rule?: Pick<IAlertRule, 'id' | 'metricType'> | null;
}

export type NewAlertEvent = Omit<IAlertEvent, 'id'> & { id: null };
