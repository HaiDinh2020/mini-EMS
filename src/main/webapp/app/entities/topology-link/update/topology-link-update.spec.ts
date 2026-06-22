import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IDevice } from 'app/entities/device/device.model';
import { DeviceService } from 'app/entities/device/service/device.service';
import { TopologyLinkService } from '../service/topology-link.service';
import { ITopologyLink } from '../topology-link.model';

import { TopologyLinkFormService } from './topology-link-form.service';
import { TopologyLinkUpdate } from './topology-link-update';

describe('TopologyLink Management Update Component', () => {
  let comp: TopologyLinkUpdate;
  let fixture: ComponentFixture<TopologyLinkUpdate>;
  let activatedRoute: ActivatedRoute;
  let topologyLinkFormService: TopologyLinkFormService;
  let topologyLinkService: TopologyLinkService;
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

    fixture = TestBed.createComponent(TopologyLinkUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    topologyLinkFormService = TestBed.inject(TopologyLinkFormService);
    topologyLinkService = TestBed.inject(TopologyLinkService);
    deviceService = TestBed.inject(DeviceService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Device query and add missing value', () => {
      const topologyLink: ITopologyLink = { id: '91bcb860-1fea-4744-ac99-89b422f2d669' };
      const sourceDevice: IDevice = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      topologyLink.sourceDevice = sourceDevice;
      const targetDevice: IDevice = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      topologyLink.targetDevice = targetDevice;

      const deviceCollection: IDevice[] = [{ id: 'd5008764-aa30-42e8-a82f-8025612527af' }];
      vitest.spyOn(deviceService, 'query').mockReturnValue(of(new HttpResponse({ body: deviceCollection })));
      const additionalDevices = [sourceDevice, targetDevice];
      const expectedCollection: IDevice[] = [...additionalDevices, ...deviceCollection];
      vitest.spyOn(deviceService, 'addDeviceToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ topologyLink });
      comp.ngOnInit();

      expect(deviceService.query).toHaveBeenCalled();
      expect(deviceService.addDeviceToCollectionIfMissing).toHaveBeenCalledWith(
        deviceCollection,
        ...additionalDevices.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.devicesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const topologyLink: ITopologyLink = { id: '91bcb860-1fea-4744-ac99-89b422f2d669' };
      const sourceDevice: IDevice = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      topologyLink.sourceDevice = sourceDevice;
      const targetDevice: IDevice = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
      topologyLink.targetDevice = targetDevice;

      activatedRoute.data = of({ topologyLink });
      comp.ngOnInit();

      expect(comp.devicesSharedCollection()).toContainEqual(sourceDevice);
      expect(comp.devicesSharedCollection()).toContainEqual(targetDevice);
      expect(comp.topologyLink).toEqual(topologyLink);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ITopologyLink>();
      const topologyLink = { id: '118ecf33-e7af-464b-a0ee-70a59f67f520' };
      vitest.spyOn(topologyLinkFormService, 'getTopologyLink').mockReturnValue(topologyLink);
      vitest.spyOn(topologyLinkService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ topologyLink });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(topologyLink);
      saveSubject.complete();

      // THEN
      expect(topologyLinkFormService.getTopologyLink).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(topologyLinkService.update).toHaveBeenCalledWith(expect.objectContaining(topologyLink));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ITopologyLink>();
      const topologyLink = { id: '118ecf33-e7af-464b-a0ee-70a59f67f520' };
      vitest.spyOn(topologyLinkFormService, 'getTopologyLink').mockReturnValue({ id: null });
      vitest.spyOn(topologyLinkService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ topologyLink: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(topologyLink);
      saveSubject.complete();

      // THEN
      expect(topologyLinkFormService.getTopologyLink).toHaveBeenCalled();
      expect(topologyLinkService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ITopologyLink>();
      const topologyLink = { id: '118ecf33-e7af-464b-a0ee-70a59f67f520' };
      vitest.spyOn(topologyLinkService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ topologyLink });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(topologyLinkService.update).toHaveBeenCalled();
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
