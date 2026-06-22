import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IAlertEvent } from '../alert-event.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../alert-event.test-samples';

import { AlertEventService, RestAlertEvent } from './alert-event.service';

const requireRestSample: RestAlertEvent = {
  ...sampleWithRequiredData,
  triggeredAt: sampleWithRequiredData.triggeredAt?.toJSON(),
  resolvedAt: sampleWithRequiredData.resolvedAt?.toJSON(),
};

describe('AlertEvent Service', () => {
  let service: AlertEventService;
  let httpMock: HttpTestingController;
  let expectedResult: IAlertEvent | IAlertEvent[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(AlertEventService);
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

    it('should create a AlertEvent', () => {
      const alertEvent = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(alertEvent).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a AlertEvent', () => {
      const alertEvent = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(alertEvent).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a AlertEvent', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of AlertEvent', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a AlertEvent', () => {
      service.delete('ABC').subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addAlertEventToCollectionIfMissing', () => {
      it('should add a AlertEvent to an empty array', () => {
        const alertEvent: IAlertEvent = sampleWithRequiredData;
        expectedResult = service.addAlertEventToCollectionIfMissing([], alertEvent);
        expect(expectedResult).toEqual([alertEvent]);
      });

      it('should not add a AlertEvent to an array that contains it', () => {
        const alertEvent: IAlertEvent = sampleWithRequiredData;
        const alertEventCollection: IAlertEvent[] = [
          {
            ...alertEvent,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAlertEventToCollectionIfMissing(alertEventCollection, alertEvent);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a AlertEvent to an array that doesn't contain it", () => {
        const alertEvent: IAlertEvent = sampleWithRequiredData;
        const alertEventCollection: IAlertEvent[] = [sampleWithPartialData];
        expectedResult = service.addAlertEventToCollectionIfMissing(alertEventCollection, alertEvent);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(alertEvent);
      });

      it('should add only unique AlertEvent to an array', () => {
        const alertEventArray: IAlertEvent[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const alertEventCollection: IAlertEvent[] = [sampleWithRequiredData];
        expectedResult = service.addAlertEventToCollectionIfMissing(alertEventCollection, ...alertEventArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const alertEvent: IAlertEvent = sampleWithRequiredData;
        const alertEvent2: IAlertEvent = sampleWithPartialData;
        expectedResult = service.addAlertEventToCollectionIfMissing([], alertEvent, alertEvent2);
        expect(expectedResult).toEqual([alertEvent, alertEvent2]);
      });

      it('should accept null and undefined values', () => {
        const alertEvent: IAlertEvent = sampleWithRequiredData;
        expectedResult = service.addAlertEventToCollectionIfMissing([], null, alertEvent, undefined);
        expect(expectedResult).toEqual([alertEvent]);
      });

      it('should return initial array if no AlertEvent is added', () => {
        const alertEventCollection: IAlertEvent[] = [sampleWithRequiredData];
        expectedResult = service.addAlertEventToCollectionIfMissing(alertEventCollection, undefined, null);
        expect(expectedResult).toEqual(alertEventCollection);
      });
    });

    describe('compareAlertEvent', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAlertEvent(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: '9804ef1a-eec2-47f1-b576-cdfa1e0d89d1' };
        const entity2 = null;

        const compareResult1 = service.compareAlertEvent(entity1, entity2);
        const compareResult2 = service.compareAlertEvent(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: '9804ef1a-eec2-47f1-b576-cdfa1e0d89d1' };
        const entity2 = { id: 'f723ab9c-43a8-4b87-9dba-04862f632f57' };

        const compareResult1 = service.compareAlertEvent(entity1, entity2);
        const compareResult2 = service.compareAlertEvent(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: '9804ef1a-eec2-47f1-b576-cdfa1e0d89d1' };
        const entity2 = { id: '9804ef1a-eec2-47f1-b576-cdfa1e0d89d1' };

        const compareResult1 = service.compareAlertEvent(entity1, entity2);
        const compareResult2 = service.compareAlertEvent(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
