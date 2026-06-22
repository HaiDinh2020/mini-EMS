import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { faArrowLeft, faPencilAlt } from '@fortawesome/free-solid-svg-icons';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { AlertRuleDetail } from './alert-rule-detail';

describe('AlertRule Management Detail Component', () => {
  let comp: AlertRuleDetail;
  let fixture: ComponentFixture<AlertRuleDetail>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./alert-rule-detail').then(m => m.AlertRuleDetail),
              resolve: { alertRule: () => of({ id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' }) },
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
    fixture = TestBed.createComponent(AlertRuleDetail);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load alertRule on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', AlertRuleDetail);

      // THEN
      expect(instance.alertRule()).toEqual(expect.objectContaining({ id: '3410c5dd-5c03-47e7-8879-77d7e4199ac7' }));
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
