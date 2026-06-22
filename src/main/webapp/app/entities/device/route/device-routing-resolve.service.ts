import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IDevice } from '../device.model';
import { DeviceService } from '../service/device.service';

const deviceResolve = (route: ActivatedRouteSnapshot): Observable<null | IDevice> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(DeviceService);
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

export default deviceResolve;
