import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import DeviceResolve from './route/device-routing-resolve.service';

const deviceRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/device').then(m => m.Device),
    data: {},
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/device-detail').then(m => m.DeviceDetail),
    resolve: {
      device: DeviceResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/device-update').then(m => m.DeviceUpdate),
    resolve: {
      device: DeviceResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/device-update').then(m => m.DeviceUpdate),
    resolve: {
      device: DeviceResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default deviceRoute;
