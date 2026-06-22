import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { faArrowLeft, faPencilAlt } from '@fortawesome/free-solid-svg-icons';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { TopologyLinkDetail } from './topology-link-detail';

describe('TopologyLink Management Detail Component', () => {
  let comp: TopologyLinkDetail;
  let fixture: ComponentFixture<TopologyLinkDetail>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./topology-link-detail').then(m => m.TopologyLinkDetail),
              resolve: { topologyLink: () => of({ id: '118ecf33-e7af-464b-a0ee-70a59f67f520' }) },
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
    fixture = TestBed.createComponent(TopologyLinkDetail);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load topologyLink on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', TopologyLinkDetail);

      // THEN
      expect(instance.topologyLink()).toEqual(expect.objectContaining({ id: '118ecf33-e7af-464b-a0ee-70a59f67f520' }));
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
