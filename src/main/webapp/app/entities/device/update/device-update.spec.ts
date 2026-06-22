import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { ICredential } from 'app/entities/credential/credential.model';
import { CredentialService } from 'app/entities/credential/service/credential.service';
import { IDevice } from '../device.model';
import { DeviceService } from '../service/device.service';

import { DeviceFormService } from './device-form.service';
import { DeviceUpdate } from './device-update';

describe('Device Management Update Component', () => {
  let comp: DeviceUpdate;
  let fixture: ComponentFixture<DeviceUpdate>;
  let activatedRoute: ActivatedRoute;
  let deviceFormService: DeviceFormService;
  let deviceService: DeviceService;
  let credentialService: CredentialService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    });

    fixture = TestBed.createComponent(DeviceUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    deviceFormService = TestBed.inject(DeviceFormService);
    deviceService = TestBed.inject(DeviceService);
    credentialService = TestBed.inject(CredentialService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Credential query and add missing value', () => {
      const device: IDevice = { id: '00bc1f66-dde7-4d14-8a96-54a882834ea4' };
      const credential: ICredential = { id: '35b3b582-8e66-4c2d-9e4a-8ff9d99022d0' };
      device.credential = credential;

      const credentialCollection: ICredential[] = [{ id: '35b3b582-8e66-4c2d-9e4a-8ff9d99022d0' }];
      vitest.spyOn(credentialService, 'query').mockReturnValue(of(new HttpResponse({ body: credentialCollection })));
      const additionalCredentials = [credential];
      const expectedCollection: ICredential[] = [...additionalCredentials, ...credentialCollection];
      vitest.spyOn(credentialService, 'addCredentialToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ device });
      comp.ngOnInit();

      expect(credentialService.query).toHaveBeenCalled();
      expect(credentialService.addCredentialToCollectionIfMissing).toHaveBeenCalledWith(
        credentialCollection,
        ...additionalCredentials.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.credentialsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const device: IDevice = { id: '00bc1f66-dde7-4d14-8a96-54a882834ea4' };
      const credential: ICredential = { id: '35b3b582-8e66-4c2d-9e4a-8ff9d99022d0' };
      device.credential = credential;

      activatedRoute.data = of({ device });
      comp.ngOnInit();

      expect(comp.credentialsSharedCollection()).toContainEqual(credential);
      expect(comp.device).toEqual(device);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IDevice>();
      const device = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      vitest.spyOn(deviceFormService, 'getDevice').mockReturnValue(device);
      vitest.spyOn(deviceService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ device });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(device);
      saveSubject.complete();

      // THEN
      expect(deviceFormService.getDevice).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(deviceService.update).toHaveBeenCalledWith(expect.objectContaining(device));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IDevice>();
      const device = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      vitest.spyOn(deviceFormService, 'getDevice').mockReturnValue({ id: null });
      vitest.spyOn(deviceService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ device: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(device);
      saveSubject.complete();

      // THEN
      expect(deviceFormService.getDevice).toHaveBeenCalled();
      expect(deviceService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IDevice>();
      const device = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      vitest.spyOn(deviceService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ device });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(deviceService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareCredential', () => {
      it('should forward to credentialService', () => {
        const entity = { id: '35b3b582-8e66-4c2d-9e4a-8ff9d99022d0' };
        const entity2 = { id: '37c978bd-bd74-4bba-a58a-e21267b95005' };
        vitest.spyOn(credentialService, 'compareCredential');
        comp.compareCredential(entity, entity2);
        expect(credentialService.compareCredential).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
