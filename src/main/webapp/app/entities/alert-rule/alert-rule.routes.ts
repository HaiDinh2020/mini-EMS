import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import AlertRuleResolve from './route/alert-rule-routing-resolve.service';

const alertRuleRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/alert-rule').then(m => m.AlertRule),
    data: { authorities: ['ROLE_USER', 'ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/alert-rule-detail').then(m => m.AlertRuleDetail),
    resolve: {
      alertRule: AlertRuleResolve,
    },
    data: { authorities: ['ROLE_USER', 'ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/alert-rule-update').then(m => m.AlertRuleUpdate),
    resolve: {
      alertRule: AlertRuleResolve,
    },
    data: { authorities: ['ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/alert-rule-update').then(m => m.AlertRuleUpdate),
    resolve: {
      alertRule: AlertRuleResolve,
    },
    data: { authorities: ['ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
  },
];

export default alertRuleRoute;
