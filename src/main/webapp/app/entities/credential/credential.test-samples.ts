import { ICredential, NewCredential } from './credential.model';

export const sampleWithRequiredData: ICredential = {
  id: '751a530f-4678-4946-8e32-2bebd823c75d',
  name: 'towards elegantly unaware',
  authType: 'SSH_KEY',
  username: 'adventurously absent quicker',
  encryptedSecret: 'after parsnip',
};

export const sampleWithPartialData: ICredential = {
  id: 'd9c22a28-6296-495f-9176-b0d075f6c86c',
  name: 'square',
  authType: 'SSH_KEY',
  username: 'um',
  encryptedSecret: 'corporation naughty',
};

export const sampleWithFullData: ICredential = {
  id: '5e0607ea-e22a-4c24-94fe-18e9bbea340f',
  name: 'while',
  authType: 'SSH_KEY',
  username: 'recede',
  encryptedSecret: 'qua',
};

export const sampleWithNewData: NewCredential = {
  name: 'jell fax meh',
  authType: 'SSH_KEY',
  username: 'banish below',
  encryptedSecret: 'or coordination',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
