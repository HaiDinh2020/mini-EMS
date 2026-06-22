import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../topology-link.test-samples';

import { TopologyLinkFormService } from './topology-link-form.service';

describe('TopologyLink Form Service', () => {
  let service: TopologyLinkFormService;

  beforeEach(() => {
    service = TestBed.inject(TopologyLinkFormService);
  });

  describe('Service methods', () => {
    describe('createTopologyLinkFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createTopologyLinkFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            linkType: expect.any(Object),
            bandwidthMbps: expect.any(Object),
            status: expect.any(Object),
            sourceDevice: expect.any(Object),
            targetDevice: expect.any(Object),
          }),
        );
      });

      it('passing ITopologyLink should create a new form with FormGroup', () => {
        const formGroup = service.createTopologyLinkFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            linkType: expect.any(Object),
            bandwidthMbps: expect.any(Object),
            status: expect.any(Object),
            sourceDevice: expect.any(Object),
            targetDevice: expect.any(Object),
          }),
        );
      });
    });

    describe('getTopologyLink', () => {
      it('should return NewTopologyLink for default TopologyLink initial value', () => {
        const formGroup = service.createTopologyLinkFormGroup(sampleWithNewData);

        const topologyLink = service.getTopologyLink(formGroup);

        expect(topologyLink).toMatchObject(sampleWithNewData);
      });

      it('should return NewTopologyLink for empty TopologyLink initial value', () => {
        const formGroup = service.createTopologyLinkFormGroup();

        const topologyLink = service.getTopologyLink(formGroup);

        expect(topologyLink).toMatchObject({});
      });

      it('should return ITopologyLink', () => {
        const formGroup = service.createTopologyLinkFormGroup(sampleWithRequiredData);

        const topologyLink = service.getTopologyLink(formGroup);

        expect(topologyLink).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ITopologyLink should not enable id FormControl', () => {
        const formGroup = service.createTopologyLinkFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewTopologyLink should disable id FormControl', () => {
        const formGroup = service.createTopologyLinkFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
