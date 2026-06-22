import { IAlertRule, NewAlertRule } from './alert-rule.model';

export const sampleWithRequiredData: IAlertRule = {
  id: '04358280-1cdc-429c-91ac-c87429009865',
  metricType: 'CPU',
  thresholdWarning: 27494.49,
  thresholdCritical: 19218.12,
  enabled: true,
};

export const sampleWithPartialData: IAlertRule = {
  id: 'a206e5df-93c1-4285-a936-4d9f89258745',
  metricType: 'CPU',
  thresholdWarning: 27808.43,
  thresholdCritical: 29867.82,
  enabled: false,
};

export const sampleWithFullData: IAlertRule = {
  id: 'd3a14045-c622-40cd-9fc6-b3616971b831',
  metricType: 'PING_LATENCY',
  thresholdWarning: 344.21,
  thresholdCritical: 12607.43,
  enabled: false,
};

export const sampleWithNewData: NewAlertRule = {
  metricType: 'RAM',
  thresholdWarning: 5337.61,
  thresholdCritical: 23503.01,
  enabled: true,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
