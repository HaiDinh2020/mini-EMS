import dayjs from 'dayjs/esm';

export interface IAuditLog {
  id: string;
  username?: string | null;
  action?: string | null;
  entityName?: string | null;
  entityId?: string | null;
  detail?: string | null;
  timestamp?: dayjs.Dayjs | null;
}

export type NewAuditLog = Omit<IAuditLog, 'id'> & { id: null };
