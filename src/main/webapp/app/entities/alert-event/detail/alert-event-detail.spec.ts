import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { faArrowLeft, faPencilAlt } from '@fortawesome/free-solid-svg-icons';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { AlertEventDetail } from './alert-event-detail';

describe('AlertEvent Management Detail Component', () => {
  let comp: AlertEventDetail;
  let fixture: ComponentFixture<AlertEventDetail>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./alert-event-detail').then(m => m.AlertEventDetail),
              resolve: { alertEvent: () => of({ id: '9804ef1a-eec2-47f1-b576-cdfa1e0d89d1' }) },
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
    fixture = TestBed.createComponent(AlertEventDetail);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load alertEvent on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', AlertEventDetail);

      // THEN
      expect(instance.alertEvent()).toEqual(expect.objectContaining({ id: '9804ef1a-eec2-47f1-b576-cdfa1e0d89d1' }));
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
