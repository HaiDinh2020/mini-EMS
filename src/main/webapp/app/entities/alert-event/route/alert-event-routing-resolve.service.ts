import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IAlertEvent } from '../alert-event.model';
import { AlertEventService } from '../service/alert-event.service';

const alertEventResolve = (route: ActivatedRouteSnapshot): Observable<null | IAlertEvent> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(AlertEventService);
    return service.find(id).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          router.navigate(['404']);
        } else {
          router.navigate(['error']);
        }
        return EMPTY;
      }),
    );
  }

  return of(null);
};

export default alertEventResolve;
