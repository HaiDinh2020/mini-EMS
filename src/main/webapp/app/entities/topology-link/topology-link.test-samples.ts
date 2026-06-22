import { ITopologyLink, NewTopologyLink } from './topology-link.model';

export const sampleWithRequiredData: ITopologyLink = {
  id: 'cd5e85bc-072d-4b09-ac5b-bd03a0b25137',
  status: 'DEGRADED',
};

export const sampleWithPartialData: ITopologyLink = {
  id: '85f1b004-2e2a-43ef-94c5-ea7791f6fbdd',
  linkType: 'suburban',
  bandwidthMbps: 21797.49,
  status: 'DEGRADED',
};

export const sampleWithFullData: ITopologyLink = {
  id: '81396682-ab79-4727-b875-26123f74dc01',
  linkType: 'corral',
  bandwidthMbps: 14825.72,
  status: 'DOWN',
};

export const sampleWithNewData: NewTopologyLink = {
  status: 'UP',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
