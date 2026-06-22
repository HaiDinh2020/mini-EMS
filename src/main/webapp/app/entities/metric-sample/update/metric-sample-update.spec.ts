import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IDevice } from 'app/entities/device/device.model';
import { DeviceService } from 'app/entities/device/service/device.service';
import { IMetricSample } from '../metric-sample.model';
import { MetricSampleService } from '../service/metric-sample.service';

import { MetricSampleFormService } from './metric-sample-form.service';
import { MetricSampleUpdate } from './metric-sample-update';

describe('MetricSample Management Update Component', () => {
  let comp: MetricSampleUpdate;
  let fixture: ComponentFixture<MetricSampleUpdate>;
  let activatedRoute: ActivatedRoute;
  let metricSampleFormService: MetricSampleFormService;
  let metricSampleService: MetricSampleService;
  let deviceService: DeviceService;

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

    fixture = TestBed.createComponent(MetricSampleUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    metricSampleFormService = TestBed.inject(MetricSampleFormService);
    metricSampleService = TestBed.inject(MetricSampleService);
    deviceService = TestBed.inject(DeviceService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Device query and add missing value', () => {
      const metricSample: IMetricSample = { id: 'd3497534-9ce4-4ec6-90cb-adb4fbb7a003' };
      const device: IDevice = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      metricSample.device = device;

      const deviceCollection: IDevice[] = [{ id: 'd5008764-aa30-42e8-a82f-8025612527af' }];
      vitest.spyOn(deviceService, 'query').mockReturnValue(of(new HttpResponse({ body: deviceCollection })));
      const additionalDevices = [device];
      const expectedCollection: IDevice[] = [...additionalDevices, ...deviceCollection];
      vitest.spyOn(deviceService, 'addDeviceToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ metricSample });
      comp.ngOnInit();

      expect(deviceService.query).toHaveBeenCalled();
      expect(deviceService.addDeviceToCollectionIfMissing).toHaveBeenCalledWith(
        deviceCollection,
        ...additionalDevices.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.devicesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const metricSample: IMetricSample = { id: 'd3497534-9ce4-4ec6-90cb-adb4fbb7a003' };
      const device: IDevice = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      metricSample.device = device;

      activatedRoute.data = of({ metricSample });
      comp.ngOnInit();

      expect(comp.devicesSharedCollection()).toContainEqual(device);
      expect(comp.metricSample).toEqual(metricSample);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IMetricSample>();
      const metricSample = { id: 'fd69406b-7fef-480a-a1db-0301c5494580' };
      vitest.spyOn(metricSampleFormService, 'getMetricSample').mockReturnValue(metricSample);
      vitest.spyOn(metricSampleService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ metricSample });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(metricSample);
      saveSubject.complete();

      // THEN
      expect(metricSampleFormService.getMetricSample).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(metricSampleService.update).toHaveBeenCalledWith(expect.objectContaining(metricSample));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IMetricSample>();
      const metricSample = { id: 'fd69406b-7fef-480a-a1db-0301c5494580' };
      vitest.spyOn(metricSampleFormService, 'getMetricSample').mockReturnValue({ id: null });
      vitest.spyOn(metricSampleService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ metricSample: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(metricSample);
      saveSubject.complete();

      // THEN
      expect(metricSampleFormService.getMetricSample).toHaveBeenCalled();
      expect(metricSampleService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IMetricSample>();
      const metricSample = { id: 'fd69406b-7fef-480a-a1db-0301c5494580' };
      vitest.spyOn(metricSampleService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ metricSample });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(metricSampleService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareDevice', () => {
      it('should forward to deviceService', () => {
        const entity = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
        const entity2 = { id: '00bc1f66-dde7-4d14-8a96-54a882834ea4' };
        vitest.spyOn(deviceService, 'compareDevice');
        comp.compareDevice(entity, entity2);
        expect(deviceService.compareDevice).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
