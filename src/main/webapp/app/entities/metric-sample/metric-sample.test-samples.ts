import dayjs from 'dayjs/esm';

import { IMetricSample, NewMetricSample } from './metric-sample.model';

export const sampleWithRequiredData: IMetricSample = {
  id: 'dc11fcb8-4fe2-48d1-aede-15455c2599fd',
  collectedAt: dayjs('2026-06-19T00:27'),
};

export const sampleWithPartialData: IMetricSample = {
  id: '1b0e8f06-a683-4fe2-94d7-4d058c775c0a',
  ramUsage: 8041.13,
  diskUsage: 14283.36,
  collectedAt: dayjs('2026-06-18T17:49'),
};

export const sampleWithFullData: IMetricSample = {
  id: '16680f6f-e33a-43a6-8011-f19f9acea665',
  cpuUsage: 26288.89,
  ramUsage: 336.69,
  diskUsage: 82.09,
  pingLatencyMs: 27524.72,
  collectedAt: dayjs('2026-06-19T01:49'),
};

export const sampleWithNewData: NewMetricSample = {
  collectedAt: dayjs('2026-06-19T03:58'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
