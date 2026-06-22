import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { TopologyLinkService } from '../service/topology-link.service';
import { ITopologyLink } from '../topology-link.model';

const topologyLinkResolve = (route: ActivatedRouteSnapshot): Observable<null | ITopologyLink> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(TopologyLinkService);
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

export default topologyLinkResolve;
