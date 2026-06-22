import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IAlertRule } from 'app/entities/alert-rule/alert-rule.model';
import { AlertRuleService } from 'app/entities/alert-rule/service/alert-rule.service';
import { IDevice } from 'app/entities/device/device.model';
import { DeviceService } from 'app/entities/device/service/device.service';
import { IAlertEvent } from '../alert-event.model';
import { AlertEventService } from '../service/alert-event.service';

import { AlertEventFormService } from './alert-event-form.service';
import { AlertEventUpdate } from './alert-event-update';

describe('AlertEvent Management Update Component', () => {
  let comp: AlertEventUpdate;
  let fixture: ComponentFixture<AlertEventUpdate>;
  let activatedRoute: ActivatedRoute;
  let alertEventFormService: AlertEventFormService;
  let alertEventService: AlertEventService;
  let deviceService: DeviceService;
  let alertRuleService: AlertRuleService;

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

    fixture = TestBed.createComponent(AlertEventUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    alertEventFormService = TestBed.inject(AlertEventFormService);
    alertEventService = TestBed.inject(AlertEventService);
    deviceService = TestBed.inject(DeviceService);
    alertRuleService = TestBed.inject(AlertRuleService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Device query and add missing value', () => {
      const alertEvent: IAlertEvent = { id: 'f723ab9c-43a8-4b87-9dba-04862f632f57' };
      const device: IDevice = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      alertEvent.device = device;

      const deviceCollection: IDevice[] = [{ id: 'd5008764-aa30-42e8-a82f-8025612527af' }];
      vitest.spyOn(deviceService, 'query').mockReturnValue(of(new HttpResponse({ body: deviceCollection })));
      const additionalDevices = [device];
      const expectedCollection: IDevice[] = [...additionalDevices, ...deviceCollection];
      vitest.spyOn(deviceService, 'addDeviceToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ alertEvent });
      comp.ngOnInit();

      expect(deviceService.query).toHaveBeenCalled();
      expect(deviceService.addDeviceToCollectionIfMissing).toHaveBeenCalledWith(
        deviceCollection,
        ...additionalDevices.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.devicesSharedCollection()).toEqual(expectedCollection);
    });

    it('should call AlertRule query and add missing value', () => {
      const alertEvent: IAlertEvent = { id: 'f723ab9c-43a8-4b87-9dba-04862f632f57' };
      const rule: IAlertRule = { id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' };
      alertEvent.rule = rule;

      const alertRuleCollection: IAlertRule[] = [{ id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' }];
      vitest.spyOn(alertRuleService, 'query').mockReturnValue(of(new HttpResponse({ body: alertRuleCollection })));
      const additionalAlertRules = [rule];
      const expectedCollection: IAlertRule[] = [...additionalAlertRules, ...alertRuleCollection];
      vitest.spyOn(alertRuleService, 'addAlertRuleToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ alertEvent });
      comp.ngOnInit();

      expect(alertRuleService.query).toHaveBeenCalled();
      expect(alertRuleService.addAlertRuleToCollectionIfMissing).toHaveBeenCalledWith(
        alertRuleCollection,
        ...additionalAlertRules.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.alertRulesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const alertEvent: IAlertEvent = { id: 'f723ab9c-43a8-4b87-9dba-04862f632f57' };
      const device: IDevice = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      alertEvent.device = device;
      const rule: IAlertRule = { id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' };
      alertEvent.rule = rule;

      activatedRoute.data = of({ alertEvent });
      comp.ngOnInit();

      expect(comp.devicesSharedCollection()).toContainEqual(device);
      expect(comp.alertRulesSharedCollection()).toContainEqual(rule);
      expect(comp.alertEvent).toEqual(alertEvent);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAlertEvent>();
      const alertEvent = { id: '9804ef1a-eec2-47f1-b576-cdfa1e0d89d1' };
      vitest.spyOn(alertEventFormService, 'getAlertEvent').mockReturnValue(alertEvent);
      vitest.spyOn(alertEventService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ alertEvent });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(alertEvent);
      saveSubject.complete();

      // THEN
      expect(alertEventFormService.getAlertEvent).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(alertEventService.update).toHaveBeenCalledWith(expect.objectContaining(alertEvent));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAlertEvent>();
      const alertEvent = { id: '9804ef1a-eec2-47f1-b576-cdfa1e0d89d1' };
      vitest.spyOn(alertEventFormService, 'getAlertEvent').mockReturnValue({ id: null });
      vitest.spyOn(alertEventService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ alertEvent: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(alertEvent);
      saveSubject.complete();

      // THEN
      expect(alertEventFormService.getAlertEvent).toHaveBeenCalled();
      expect(alertEventService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IAlertEvent>();
      const alertEvent = { id: '9804ef1a-eec2-47f1-b576-cdfa1e0d89d1' };
      vitest.spyOn(alertEventService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ alertEvent });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(alertEventService.update).toHaveBeenCalled();
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

    describe('compareAlertRule', () => {
      it('should forward to alertRuleService', () => {
        const entity = { id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' };
        const entity2 = { id: '4d9b9ced-bd69-4df9-a5d7-eb14247e0d17' };
        vitest.spyOn(alertRuleService, 'compareAlertRule');
        comp.compareAlertRule(entity, entity2);
        expect(alertRuleService.compareAlertRule).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
