import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IAlertRule } from '../alert-rule.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../alert-rule.test-samples';

import { AlertRuleService } from './alert-rule.service';

const requireRestSample: IAlertRule = {
  ...sampleWithRequiredData,
};

describe('AlertRule Service', () => {
  let service: AlertRuleService;
  let httpMock: HttpTestingController;
  let expectedResult: IAlertRule | IAlertRule[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(AlertRuleService);
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

    it('should create a AlertRule', () => {
      const alertRule = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(alertRule).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a AlertRule', () => {
      const alertRule = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(alertRule).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a AlertRule', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of AlertRule', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a AlertRule', () => {
      service.delete('ABC').subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addAlertRuleToCollectionIfMissing', () => {
      it('should add a AlertRule to an empty array', () => {
        const alertRule: IAlertRule = sampleWithRequiredData;
        expectedResult = service.addAlertRuleToCollectionIfMissing([], alertRule);
        expect(expectedResult).toEqual([alertRule]);
      });

      it('should not add a AlertRule to an array that contains it', () => {
        const alertRule: IAlertRule = sampleWithRequiredData;
        const alertRuleCollection: IAlertRule[] = [
          {
            ...alertRule,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAlertRuleToCollectionIfMissing(alertRuleCollection, alertRule);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a AlertRule to an array that doesn't contain it", () => {
        const alertRule: IAlertRule = sampleWithRequiredData;
        const alertRuleCollection: IAlertRule[] = [sampleWithPartialData];
        expectedResult = service.addAlertRuleToCollectionIfMissing(alertRuleCollection, alertRule);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(alertRule);
      });

      it('should add only unique AlertRule to an array', () => {
        const alertRuleArray: IAlertRule[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const alertRuleCollection: IAlertRule[] = [sampleWithRequiredData];
        expectedResult = service.addAlertRuleToCollectionIfMissing(alertRuleCollection, ...alertRuleArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const alertRule: IAlertRule = sampleWithRequiredData;
        const alertRule2: IAlertRule = sampleWithPartialData;
        expectedResult = service.addAlertRuleToCollectionIfMissing([], alertRule, alertRule2);
        expect(expectedResult).toEqual([alertRule, alertRule2]);
      });

      it('should accept null and undefined values', () => {
        const alertRule: IAlertRule = sampleWithRequiredData;
        expectedResult = service.addAlertRuleToCollectionIfMissing([], null, alertRule, undefined);
        expect(expectedResult).toEqual([alertRule]);
      });

      it('should return initial array if no AlertRule is added', () => {
        const alertRuleCollection: IAlertRule[] = [sampleWithRequiredData];
        expectedResult = service.addAlertRuleToCollectionIfMissing(alertRuleCollection, undefined, null);
        expect(expectedResult).toEqual(alertRuleCollection);
      });
    });

    describe('compareAlertRule', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAlertRule(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' };
        const entity2 = null;

        const compareResult1 = service.compareAlertRule(entity1, entity2);
        const compareResult2 = service.compareAlertRule(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' };
        const entity2 = { id: '4d9b9ced-bd69-4df9-a5d7-eb14247e0d17' };

        const compareResult1 = service.compareAlertRule(entity1, entity2);
        const compareResult2 = service.compareAlertRule(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' };
        const entity2 = { id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' };

        const compareResult1 = service.compareAlertRule(entity1, entity2);
        const compareResult2 = service.compareAlertRule(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
