import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../metric-sample.test-samples';

import { MetricSampleFormService } from './metric-sample-form.service';

describe('MetricSample Form Service', () => {
  let service: MetricSampleFormService;

  beforeEach(() => {
    service = TestBed.inject(MetricSampleFormService);
  });

  describe('Service methods', () => {
    describe('createMetricSampleFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createMetricSampleFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            cpuUsage: expect.any(Object),
            ramUsage: expect.any(Object),
            diskUsage: expect.any(Object),
            pingLatencyMs: expect.any(Object),
            collectedAt: expect.any(Object),
            device: expect.any(Object),
          }),
        );
      });

      it('passing IMetricSample should create a new form with FormGroup', () => {
        const formGroup = service.createMetricSampleFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            cpuUsage: expect.any(Object),
            ramUsage: expect.any(Object),
            diskUsage: expect.any(Object),
            pingLatencyMs: expect.any(Object),
            collectedAt: expect.any(Object),
            device: expect.any(Object),
          }),
        );
      });
    });

    describe('getMetricSample', () => {
      it('should return NewMetricSample for default MetricSample initial value', () => {
        const formGroup = service.createMetricSampleFormGroup(sampleWithNewData);

        const metricSample = service.getMetricSample(formGroup);

        expect(metricSample).toMatchObject(sampleWithNewData);
      });

      it('should return NewMetricSample for empty MetricSample initial value', () => {
        const formGroup = service.createMetricSampleFormGroup();

        const metricSample = service.getMetricSample(formGroup);

        expect(metricSample).toMatchObject({});
      });

      it('should return IMetricSample', () => {
        const formGroup = service.createMetricSampleFormGroup(sampleWithRequiredData);

        const metricSample = service.getMetricSample(formGroup);

        expect(metricSample).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IMetricSample should not enable id FormControl', () => {
        const formGroup = service.createMetricSampleFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewMetricSample should disable id FormControl', () => {
        const formGroup = service.createMetricSampleFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
