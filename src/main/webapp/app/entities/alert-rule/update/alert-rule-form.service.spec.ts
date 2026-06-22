import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../alert-rule.test-samples';

import { AlertRuleFormService } from './alert-rule-form.service';

describe('AlertRule Form Service', () => {
  let service: AlertRuleFormService;

  beforeEach(() => {
    service = TestBed.inject(AlertRuleFormService);
  });

  describe('Service methods', () => {
    describe('createAlertRuleFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAlertRuleFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            metricType: expect.any(Object),
            thresholdWarning: expect.any(Object),
            thresholdCritical: expect.any(Object),
            enabled: expect.any(Object),
          }),
        );
      });

      it('passing IAlertRule should create a new form with FormGroup', () => {
        const formGroup = service.createAlertRuleFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            metricType: expect.any(Object),
            thresholdWarning: expect.any(Object),
            thresholdCritical: expect.any(Object),
            enabled: expect.any(Object),
          }),
        );
      });
    });

    describe('getAlertRule', () => {
      it('should return NewAlertRule for default AlertRule initial value', () => {
        const formGroup = service.createAlertRuleFormGroup(sampleWithNewData);

        const alertRule = service.getAlertRule(formGroup);

        expect(alertRule).toMatchObject(sampleWithNewData);
      });

      it('should return NewAlertRule for empty AlertRule initial value', () => {
        const formGroup = service.createAlertRuleFormGroup();

        const alertRule = service.getAlertRule(formGroup);

        expect(alertRule).toMatchObject({});
      });

      it('should return IAlertRule', () => {
        const formGroup = service.createAlertRuleFormGroup(sampleWithRequiredData);

        const alertRule = service.getAlertRule(formGroup);

        expect(alertRule).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAlertRule should not enable id FormControl', () => {
        const formGroup = service.createAlertRuleFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAlertRule should disable id FormControl', () => {
        const formGroup = service.createAlertRuleFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
