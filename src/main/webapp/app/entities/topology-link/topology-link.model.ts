import { IDevice } from 'app/entities/device/device.model';
import { LinkStatus } from 'app/entities/enumerations/link-status.model';

export interface ITopologyLink {
  id: string;
  linkType?: string | null;
  bandwidthMbps?: number | null;
  status?: keyof typeof LinkStatus | null;
  sourceDevice?: Pick<IDevice, 'id' | 'name'> | null;
  targetDevice?: Pick<IDevice, 'id' | 'name'> | null;
}

export type NewTopologyLink = Omit<ITopologyLink, 'id'> & { id: null };
