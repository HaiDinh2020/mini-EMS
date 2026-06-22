import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IMetricSample } from '../metric-sample.model';
import { MetricSampleService } from '../service/metric-sample.service';

const metricSampleResolve = (route: ActivatedRouteSnapshot): Observable<null | IMetricSample> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(MetricSampleService);
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

export default metricSampleResolve;
