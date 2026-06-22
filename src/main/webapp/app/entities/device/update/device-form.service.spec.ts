import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../device.test-samples';

import { DeviceFormService } from './device-form.service';

describe('Device Form Service', () => {
  let service: DeviceFormService;

  beforeEach(() => {
    service = TestBed.inject(DeviceFormService);
  });

  describe('Service methods', () => {
    describe('createDeviceFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createDeviceFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            ipAddress: expect.any(Object),
            hostname: expect.any(Object),
            deviceType: expect.any(Object),
            vendor: expect.any(Object),
            model: expect.any(Object),
            sshPort: expect.any(Object),
            sshUsername: expect.any(Object),
            location: expect.any(Object),
            status: expect.any(Object),
            lastCheckedAt: expect.any(Object),
            monitoringEnabled: expect.any(Object),
            description: expect.any(Object),
            credential: expect.any(Object),
          }),
        );
      });

      it('passing IDevice should create a new form with FormGroup', () => {
        const formGroup = service.createDeviceFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            ipAddress: expect.any(Object),
            hostname: expect.any(Object),
            deviceType: expect.any(Object),
            vendor: expect.any(Object),
            model: expect.any(Object),
            sshPort: expect.any(Object),
            sshUsername: expect.any(Object),
            location: expect.any(Object),
            status: expect.any(Object),
            lastCheckedAt: expect.any(Object),
            monitoringEnabled: expect.any(Object),
            description: expect.any(Object),
            credential: expect.any(Object),
          }),
        );
      });
    });

    describe('getDevice', () => {
      it('should return NewDevice for default Device initial value', () => {
        const formGroup = service.createDeviceFormGroup(sampleWithNewData);

        const device = service.getDevice(formGroup);

        expect(device).toMatchObject(sampleWithNewData);
      });

      it('should return NewDevice for empty Device initial value', () => {
        const formGroup = service.createDeviceFormGroup();

        const device = service.getDevice(formGroup);

        expect(device).toMatchObject({});
      });

      it('should return IDevice', () => {
        const formGroup = service.createDeviceFormGroup(sampleWithRequiredData);

        const device = service.getDevice(formGroup);

        expect(device).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IDevice should not enable id FormControl', () => {
        const formGroup = service.createDeviceFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewDevice should disable id FormControl', () => {
        const formGroup = service.createDeviceFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
