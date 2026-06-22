import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IDevice } from '../device.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../device.test-samples';

import { DeviceService, RestDevice } from './device.service';

const requireRestSample: RestDevice = {
  ...sampleWithRequiredData,
  lastCheckedAt: sampleWithRequiredData.lastCheckedAt?.toJSON(),
};

describe('Device Service', () => {
  let service: DeviceService;
  let httpMock: HttpTestingController;
  let expectedResult: IDevice | IDevice[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(DeviceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find('ABC').subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Device', () => {
      const device = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(device).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Device', () => {
      const device = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(device).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Device', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Device', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Device', () => {
      service.delete('ABC').subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addDeviceToCollectionIfMissing', () => {
      it('should add a Device to an empty array', () => {
        const device: IDevice = sampleWithRequiredData;
        expectedResult = service.addDeviceToCollectionIfMissing([], device);
        expect(expectedResult).toEqual([device]);
      });

      it('should not add a Device to an array that contains it', () => {
        const device: IDevice = sampleWithRequiredData;
        const deviceCollection: IDevice[] = [
          {
            ...device,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addDeviceToCollectionIfMissing(deviceCollection, device);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Device to an array that doesn't contain it", () => {
        const device: IDevice = sampleWithRequiredData;
        const deviceCollection: IDevice[] = [sampleWithPartialData];
        expectedResult = service.addDeviceToCollectionIfMissing(deviceCollection, device);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(device);
      });

      it('should add only unique Device to an array', () => {
        const deviceArray: IDevice[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const deviceCollection: IDevice[] = [sampleWithRequiredData];
        expectedResult = service.addDeviceToCollectionIfMissing(deviceCollection, ...deviceArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const device: IDevice = sampleWithRequiredData;
        const device2: IDevice = sampleWithPartialData;
        expectedResult = service.addDeviceToCollectionIfMissing([], device, device2);
        expect(expectedResult).toEqual([device, device2]);
      });

      it('should accept null and undefined values', () => {
        const device: IDevice = sampleWithRequiredData;
        expectedResult = service.addDeviceToCollectionIfMissing([], null, device, undefined);
        expect(expectedResult).toEqual([device]);
      });

      it('should return initial array if no Device is added', () => {
        const deviceCollection: IDevice[] = [sampleWithRequiredData];
        expectedResult = service.addDeviceToCollectionIfMissing(deviceCollection, undefined, null);
        expect(expectedResult).toEqual(deviceCollection);
      });
    });

    describe('compareDevice', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareDevice(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
        const entity2 = null;

        const compareResult1 = service.compareDevice(entity1, entity2);
        const compareResult2 = service.compareDevice(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
        const entity2 = { id: '00bc1f66-dde7-4d14-8a96-54a882834ea4' };

        const compareResult1 = service.compareDevice(entity1, entity2);
        const compareResult2 = service.compareDevice(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };
        const entity2 = { id: 'd5008764-aa30-42e8-a82f-8025612527af' };

        const compareResult1 = service.compareDevice(entity1, entity2);
        const compareResult2 = service.compareDevice(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
