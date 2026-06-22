import { MetricType } from 'app/entities/enumerations/metric-type.model';

export interface IAlertRule {
  id: string;
  metricType?: keyof typeof MetricType | null;
  thresholdWarning?: number | null;
  thresholdCritical?: number | null;
  enabled?: boolean | null;
}

export type NewAlertRule = Omit<IAlertRule, 'id'> & { id: null };
