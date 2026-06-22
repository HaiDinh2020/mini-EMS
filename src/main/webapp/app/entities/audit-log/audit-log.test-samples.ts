import dayjs from 'dayjs/esm';

import { IAuditLog, NewAuditLog } from './audit-log.model';

export const sampleWithRequiredData: IAuditLog = {
  id: 'b6cf7b9c-c00f-45a2-8438-7ace097d2777',
  username: 'amid',
  action: 'mid of',
  entityName: 'like status intently',
  entityId: 'behest',
  timestamp: dayjs('2026-06-19T02:42'),
};

export const sampleWithPartialData: IAuditLog = {
  id: '30096318-3a85-43fc-a225-62d3bccd6375',
  username: 'frizzy out oh',
  action: 'than',
  entityName: 'penalise writhing',
  entityId: 'er through bleach',
  detail: '../fake-data/blob/hipster.txt',
  timestamp: dayjs('2026-06-18T18:52'),
};

export const sampleWithFullData: IAuditLog = {
  id: '8e87c99c-58cd-4f22-b167-47ed80d48cda',
  username: 'pish',
  action: 'hm against',
  entityName: 'towards hm',
  entityId: 'mmm extent incidentally',
  detail: '../fake-data/blob/hipster.txt',
  timestamp: dayjs('2026-06-18T14:24'),
};

export const sampleWithNewData: NewAuditLog = {
  username: 'far apricot',
  action: 'gosh garage',
  entityName: 'once fowl aha',
  entityId: 'furthermore abaft amidst',
  timestamp: dayjs('2026-06-18T15:33'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
