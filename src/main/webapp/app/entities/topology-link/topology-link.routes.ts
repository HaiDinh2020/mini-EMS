import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import TopologyLinkResolve from './route/topology-link-routing-resolve.service';

const topologyLinkRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/topology-link').then(m => m.TopologyLink),
    data: {},
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/topology-link-detail').then(m => m.TopologyLinkDetail),
    resolve: {
      topologyLink: TopologyLinkResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/topology-link-update').then(m => m.TopologyLinkUpdate),
    resolve: {
      topologyLink: TopologyLinkResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/topology-link-update').then(m => m.TopologyLinkUpdate),
    resolve: {
      topologyLink: TopologyLinkResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default topologyLinkRoute;
