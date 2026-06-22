import dayjs from 'dayjs/esm';

import { IDevice, NewDevice } from './device.model';

export const sampleWithRequiredData: IDevice = {
  id: '1b4c12f8-9525-40d0-b279-afba8e721961',
  name: 'whether upon',
  ipAddress: 'ouch',
  deviceType: 'GNODEB',
  status: 'OFFLINE',
  monitoringEnabled: true,
};

export const sampleWithPartialData: IDevice = {
  id: '9f0412e5-d8d1-462a-a0d1-ed9ea74ebd10',
  name: 'self-reliant and',
  ipAddress: 'nab except',
  hostname: 'tangible minus',
  deviceType: 'SMF',
  sshPort: 7075,
  sshUsername: 'zesty toward unknown',
  location: 'corner',
  status: 'CRITICAL',
  lastCheckedAt: dayjs('2026-06-18T08:18'),
  monitoringEnabled: false,
};

export const sampleWithFullData: IDevice = {
  id: 'a5d8f282-5455-4d32-8ef3-84c0a0cc30ba',
  name: 'vivaciously feather superior',
  ipAddress: 'instead bleach',
  hostname: 'woot times pish',
  deviceType: 'SMF',
  vendor: 'how past',
  model: 'screw',
  sshPort: 15882,
  sshUsername: 'sarong certify ashamed',
  location: 'duh productive plus',
  status: 'OFFLINE',
  lastCheckedAt: dayjs('2026-06-18T12:35'),
  monitoringEnabled: false,
  description: '../fake-data/blob/hipster.txt',
};

export const sampleWithNewData: NewDevice = {
  name: 'vice thankfully',
  ipAddress: 'sans',
  deviceType: 'SERVER',
  status: 'OFFLINE',
  monitoringEnabled: true,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
