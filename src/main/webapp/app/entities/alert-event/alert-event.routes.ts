import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import AlertEventResolve from './route/alert-event-routing-resolve.service';

const alertEventRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/alert-event').then(m => m.AlertEvent),
    data: { authorities: ['ROLE_USER', 'ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/alert-event-detail').then(m => m.AlertEventDetail),
    resolve: {
      alertEvent: AlertEventResolve,
    },
    data: { authorities: ['ROLE_USER', 'ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
];

export default alertEventRoute;
