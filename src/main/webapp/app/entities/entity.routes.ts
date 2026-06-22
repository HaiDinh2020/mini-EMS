import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'emsApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'user-management',
    data: { pageTitle: 'userManagement.home.title' },
    loadChildren: () => import('./admin/user-management/user-management.routes'),
  },
  {
    path: 'device',
    data: { pageTitle: 'emsApp.device.home.title' },
    loadChildren: () => import('./device/device.routes'),
  },
  {
    path: 'credential',
    data: { pageTitle: 'emsApp.credential.home.title' },
    loadChildren: () => import('./credential/credential.routes'),
  },
  {
    path: 'metric-sample',
    data: { pageTitle: 'emsApp.metricSample.home.title' },
    loadChildren: () => import('./metric-sample/metric-sample.routes'),
  },
  {
    path: 'alert-rule',
    data: { pageTitle: 'emsApp.alertRule.home.title' },
    loadChildren: () => import('./alert-rule/alert-rule.routes'),
  },
  {
    path: 'alert-event',
    data: { pageTitle: 'emsApp.alertEvent.home.title' },
    loadChildren: () => import('./alert-event/alert-event.routes'),
  },
  {
    path: 'topology-link',
    data: { pageTitle: 'emsApp.topologyLink.home.title' },
    loadChildren: () => import('./topology-link/topology-link.routes'),
  },
  {
    path: 'audit-log',
    data: { pageTitle: 'emsApp.auditLog.home.title' },
    loadChildren: () => import('./audit-log/audit-log.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
