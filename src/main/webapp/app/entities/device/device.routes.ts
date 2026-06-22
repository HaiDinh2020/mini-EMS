import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import DeviceResolve from './route/device-routing-resolve.service';

const deviceRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/device').then(m => m.Device),
    data: { authorities: ['ROLE_USER', 'ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/device-detail').then(m => m.DeviceDetail),
    resolve: {
      device: DeviceResolve,
    },
    data: { authorities: ['ROLE_USER', 'ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/device-update').then(m => m.DeviceUpdate),
    resolve: {
      device: DeviceResolve,
    },
    data: { authorities: ['ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/device-update').then(m => m.DeviceUpdate),
    resolve: {
      device: DeviceResolve,
    },
    data: { authorities: ['ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
];

export default deviceRoute;
