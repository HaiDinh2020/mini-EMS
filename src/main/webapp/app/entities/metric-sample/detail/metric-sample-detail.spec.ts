import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { faArrowLeft, faPencilAlt } from '@fortawesome/free-solid-svg-icons';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { MetricSampleDetail } from './metric-sample-detail';

describe('MetricSample Management Detail Component', () => {
  let comp: MetricSampleDetail;
  let fixture: ComponentFixture<MetricSampleDetail>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./metric-sample-detail').then(m => m.MetricSampleDetail),
              resolve: { metricSample: () => of({ id: 'fd69406b-7fef-480a-a1db-0301c5494580' }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    });
    const library = TestBed.inject(FaIconLibrary);
    library.addIcons(faArrowLeft);
    library.addIcons(faPencilAlt);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricSampleDetail);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load metricSample on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', MetricSampleDetail);

      // THEN
      expect(instance.metricSample()).toEqual(expect.objectContaining({ id: 'fd69406b-7fef-480a-a1db-0301c5494580' }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      vitest.spyOn(globalThis.history, 'back');
      comp.previousState();
      expect(globalThis.history.back).toHaveBeenCalled();
    });
  });
});
