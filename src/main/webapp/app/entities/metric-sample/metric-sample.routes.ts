import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import MetricSampleResolve from './route/metric-sample-routing-resolve.service';

const metricSampleRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/metric-sample').then(m => m.MetricSample),
    data: {},
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/metric-sample-detail').then(m => m.MetricSampleDetail),
    resolve: {
      metricSample: MetricSampleResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/metric-sample-update').then(m => m.MetricSampleUpdate),
    resolve: {
      metricSample: MetricSampleResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/metric-sample-update').then(m => m.MetricSampleUpdate),
    resolve: {
      metricSample: MetricSampleResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default metricSampleRoute;
