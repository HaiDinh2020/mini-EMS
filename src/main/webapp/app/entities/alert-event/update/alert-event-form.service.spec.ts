import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../alert-event.test-samples';

import { AlertEventFormService } from './alert-event-form.service';

describe('AlertEvent Form Service', () => {
  let service: AlertEventFormService;

  beforeEach(() => {
    service = TestBed.inject(AlertEventFormService);
  });

  describe('Service methods', () => {
    describe('createAlertEventFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAlertEventFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            metricType: expect.any(Object),
            value: expect.any(Object),
            severity: expect.any(Object),
            message: expect.any(Object),
            triggeredAt: expect.any(Object),
            resolvedAt: expect.any(Object),
            status: expect.any(Object),
            device: expect.any(Object),
            rule: expect.any(Object),
          }),
        );
      });

      it('passing IAlertEvent should create a new form with FormGroup', () => {
        const formGroup = service.createAlertEventFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            metricType: expect.any(Object),
            value: expect.any(Object),
            severity: expect.any(Object),
            message: expect.any(Object),
            triggeredAt: expect.any(Object),
            resolvedAt: expect.any(Object),
            status: expect.any(Object),
            device: expect.any(Object),
            rule: expect.any(Object),
          }),
        );
      });
    });

    describe('getAlertEvent', () => {
      it('should return NewAlertEvent for default AlertEvent initial value', () => {
        const formGroup = service.createAlertEventFormGroup(sampleWithNewData);

        const alertEvent = service.getAlertEvent(formGroup);

        expect(alertEvent).toMatchObject(sampleWithNewData);
      });

      it('should return NewAlertEvent for empty AlertEvent initial value', () => {
        const formGroup = service.createAlertEventFormGroup();

        const alertEvent = service.getAlertEvent(formGroup);

        expect(alertEvent).toMatchObject({});
      });

      it('should return IAlertEvent', () => {
        const formGroup = service.createAlertEventFormGroup(sampleWithRequiredData);

        const alertEvent = service.getAlertEvent(formGroup);

        expect(alertEvent).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAlertEvent should not enable id FormControl', () => {
        const formGroup = service.createAlertEventFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAlertEvent should disable id FormControl', () => {
        const formGroup = service.createAlertEventFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
