import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: 'cdbac2c8-5af0-4c05-9390-f838bceaee96',
  login: 'NhuTam_Trinh89',
};

export const sampleWithPartialData: IUser = {
  id: '09076bdf-799b-4280-a46d-d5d1b0596d4e',
  login: 'HamTho_Hoang',
};

export const sampleWithFullData: IUser = {
  id: '2c184532-359e-417e-8339-7736058f89c7',
  login: 'TrungThanh68',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
