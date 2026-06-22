import dayjs from 'dayjs/esm';

import { IAlertEvent, NewAlertEvent } from './alert-event.model';

export const sampleWithRequiredData: IAlertEvent = {
  id: '6f834869-9c27-4637-bc8e-b35fe022fcbc',
  metricType: 'RAM',
  value: 31443.23,
  severity: 'CRITICAL',
  message: 'hollow institute',
  triggeredAt: dayjs('2026-06-18T13:22'),
  status: 'ACKNOWLEDGED',
};

export const sampleWithPartialData: IAlertEvent = {
  id: '81ed8c28-a019-4a61-838a-20912f337a6a',
  metricType: 'CPU',
  value: 28132.34,
  severity: 'CRITICAL',
  message: 'yieldingly kookily',
  triggeredAt: dayjs('2026-06-19T03:20'),
  resolvedAt: dayjs('2026-06-18T16:56'),
  status: 'RESOLVED',
};

export const sampleWithFullData: IAlertEvent = {
  id: '2dea1f19-7cc5-4bae-8874-397ebc5c8507',
  metricType: 'RAM',
  value: 3229.87,
  severity: 'WARNING',
  message: 'merge deeply',
  triggeredAt: dayjs('2026-06-18T05:27'),
  resolvedAt: dayjs('2026-06-18T20:40'),
  status: 'RESOLVED',
};

export const sampleWithNewData: NewAlertEvent = {
  metricType: 'PING_LATENCY',
  value: 27109.56,
  severity: 'CRITICAL',
  message: 'impeccable boohoo',
  triggeredAt: dayjs('2026-06-18T21:09'),
  status: 'ACKNOWLEDGED',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
