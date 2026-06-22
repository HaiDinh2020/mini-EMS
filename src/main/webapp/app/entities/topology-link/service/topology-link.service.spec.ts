import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ITopologyLink } from '../topology-link.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../topology-link.test-samples';

import { TopologyLinkService } from './topology-link.service';

const requireRestSample: ITopologyLink = {
  ...sampleWithRequiredData,
};

describe('TopologyLink Service', () => {
  let service: TopologyLinkService;
  let httpMock: HttpTestingController;
  let expectedResult: ITopologyLink | ITopologyLink[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(TopologyLinkService);
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

    it('should create a TopologyLink', () => {
      const topologyLink = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(topologyLink).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a TopologyLink', () => {
      const topologyLink = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(topologyLink).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a TopologyLink', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of TopologyLink', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a TopologyLink', () => {
      service.delete('ABC').subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addTopologyLinkToCollectionIfMissing', () => {
      it('should add a TopologyLink to an empty array', () => {
        const topologyLink: ITopologyLink = sampleWithRequiredData;
        expectedResult = service.addTopologyLinkToCollectionIfMissing([], topologyLink);
        expect(expectedResult).toEqual([topologyLink]);
      });

      it('should not add a TopologyLink to an array that contains it', () => {
        const topologyLink: ITopologyLink = sampleWithRequiredData;
        const topologyLinkCollection: ITopologyLink[] = [
          {
            ...topologyLink,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addTopologyLinkToCollectionIfMissing(topologyLinkCollection, topologyLink);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a TopologyLink to an array that doesn't contain it", () => {
        const topologyLink: ITopologyLink = sampleWithRequiredData;
        const topologyLinkCollection: ITopologyLink[] = [sampleWithPartialData];
        expectedResult = service.addTopologyLinkToCollectionIfMissing(topologyLinkCollection, topologyLink);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(topologyLink);
      });

      it('should add only unique TopologyLink to an array', () => {
        const topologyLinkArray: ITopologyLink[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const topologyLinkCollection: ITopologyLink[] = [sampleWithRequiredData];
        expectedResult = service.addTopologyLinkToCollectionIfMissing(topologyLinkCollection, ...topologyLinkArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const topologyLink: ITopologyLink = sampleWithRequiredData;
        const topologyLink2: ITopologyLink = sampleWithPartialData;
        expectedResult = service.addTopologyLinkToCollectionIfMissing([], topologyLink, topologyLink2);
        expect(expectedResult).toEqual([topologyLink, topologyLink2]);
      });

      it('should accept null and undefined values', () => {
        const topologyLink: ITopologyLink = sampleWithRequiredData;
        expectedResult = service.addTopologyLinkToCollectionIfMissing([], null, topologyLink, undefined);
        expect(expectedResult).toEqual([topologyLink]);
      });

      it('should return initial array if no TopologyLink is added', () => {
        const topologyLinkCollection: ITopologyLink[] = [sampleWithRequiredData];
        expectedResult = service.addTopologyLinkToCollectionIfMissing(topologyLinkCollection, undefined, null);
        expect(expectedResult).toEqual(topologyLinkCollection);
      });
    });

    describe('compareTopologyLink', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareTopologyLink(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: '118ecf33-e7af-464b-a0ee-70a59f67f520' };
        const entity2 = null;

        const compareResult1 = service.compareTopologyLink(entity1, entity2);
        const compareResult2 = service.compareTopologyLink(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: '118ecf33-e7af-464b-a0ee-70a59f67f520' };
        const entity2 = { id: '91bcb860-1fea-4744-ac99-89b422f2d669' };

        const compareResult1 = service.compareTopologyLink(entity1, entity2);
        const compareResult2 = service.compareTopologyLink(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: '118ecf33-e7af-464b-a0ee-70a59f67f520' };
        const entity2 = { id: '118ecf33-e7af-464b-a0ee-70a59f67f520' };

        const compareResult1 = service.compareTopologyLink(entity1, entity2);
        const compareResult2 = service.compareTopologyLink(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
