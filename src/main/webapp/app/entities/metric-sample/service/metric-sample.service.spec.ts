import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IMetricSample } from '../metric-sample.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../metric-sample.test-samples';

import { MetricSampleService, RestMetricSample } from './metric-sample.service';

const requireRestSample: RestMetricSample = {
  ...sampleWithRequiredData,
  collectedAt: sampleWithRequiredData.collectedAt?.toJSON(),
};

describe('MetricSample Service', () => {
  let service: MetricSampleService;
  let httpMock: HttpTestingController;
  let expectedResult: IMetricSample | IMetricSample[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(MetricSampleService);
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

    it('should create a MetricSample', () => {
      const metricSample = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(metricSample).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a MetricSample', () => {
      const metricSample = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(metricSample).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a MetricSample', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of MetricSample', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a MetricSample', () => {
      service.delete('ABC').subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addMetricSampleToCollectionIfMissing', () => {
      it('should add a MetricSample to an empty array', () => {
        const metricSample: IMetricSample = sampleWithRequiredData;
        expectedResult = service.addMetricSampleToCollectionIfMissing([], metricSample);
        expect(expectedResult).toEqual([metricSample]);
      });

      it('should not add a MetricSample to an array that contains it', () => {
        const metricSample: IMetricSample = sampleWithRequiredData;
        const metricSampleCollection: IMetricSample[] = [
          {
            ...metricSample,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addMetricSampleToCollectionIfMissing(metricSampleCollection, metricSample);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a MetricSample to an array that doesn't contain it", () => {
        const metricSample: IMetricSample = sampleWithRequiredData;
        const metricSampleCollection: IMetricSample[] = [sampleWithPartialData];
        expectedResult = service.addMetricSampleToCollectionIfMissing(metricSampleCollection, metricSample);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(metricSample);
      });

      it('should add only unique MetricSample to an array', () => {
        const metricSampleArray: IMetricSample[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const metricSampleCollection: IMetricSample[] = [sampleWithRequiredData];
        expectedResult = service.addMetricSampleToCollectionIfMissing(metricSampleCollection, ...metricSampleArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const metricSample: IMetricSample = sampleWithRequiredData;
        const metricSample2: IMetricSample = sampleWithPartialData;
        expectedResult = service.addMetricSampleToCollectionIfMissing([], metricSample, metricSample2);
        expect(expectedResult).toEqual([metricSample, metricSample2]);
      });

      it('should accept null and undefined values', () => {
        const metricSample: IMetricSample = sampleWithRequiredData;
        expectedResult = service.addMetricSampleToCollectionIfMissing([], null, metricSample, undefined);
        expect(expectedResult).toEqual([metricSample]);
      });

      it('should return initial array if no MetricSample is added', () => {
        const metricSampleCollection: IMetricSample[] = [sampleWithRequiredData];
        expectedResult = service.addMetricSampleToCollectionIfMissing(metricSampleCollection, undefined, null);
        expect(expectedResult).toEqual(metricSampleCollection);
      });
    });

    describe('compareMetricSample', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareMetricSample(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 'fd69406b-7fef-480a-a1db-0301c5494580' };
        const entity2 = null;

        const compareResult1 = service.compareMetricSample(entity1, entity2);
        const compareResult2 = service.compareMetricSample(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 'fd69406b-7fef-480a-a1db-0301c5494580' };
        const entity2 = { id: 'd3497534-9ce4-4ec6-90cb-adb4fbb7a003' };

        const compareResult1 = service.compareMetricSample(entity1, entity2);
        const compareResult2 = service.compareMetricSample(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 'fd69406b-7fef-480a-a1db-0301c5494580' };
        const entity2 = { id: 'fd69406b-7fef-480a-a1db-0301c5494580' };

        const compareResult1 = service.compareMetricSample(entity1, entity2);
        const compareResult2 = service.compareMetricSample(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
