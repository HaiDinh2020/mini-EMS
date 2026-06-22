import { AuthType } from 'app/entities/enumerations/auth-type.model';

export interface ICredential {
  id: string;
  name?: string | null;
  authType?: keyof typeof AuthType | null;
  username?: string | null;
  encryptedSecret?: string | null;
}

export type NewCredential = Omit<ICredential, 'id'> & { id: null };
