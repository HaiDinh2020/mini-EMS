import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IAlertRule } from 'app/entities/alert-rule/alert-rule.model';
import { AlertRuleService } from 'app/entities/alert-rule/service/alert-rule.service';
import { IDevice } from 'app/entities/device/device.model';
import { DeviceService } from 'app/entities/device/service/device.service';
import { AlertStatus } from 'app/entities/enumerations/alert-status.model';
import { MetricType } from 'app/entities/enumerations/metric-type.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';

import { IAlertEvent } from '../alert-event.model';
import { AlertEventService } from '../service/alert-event.service';

import { AlertEventFormGroup, AlertEventFormService } from './alert-event-form.service';
import { Severity } from 'app/entities/enumerations/severity.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-alert-event-update',
  templateUrl: './alert-event-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class AlertEventUpdate implements OnInit {
  readonly isSaving = signal(false);
  alertEvent: IAlertEvent | null = null;
  metricTypeValues = Object.keys(MetricType);
  severityValues = Object.keys(Severity);
  alertStatusValues = Object.keys(AlertStatus);

  devicesSharedCollection = signal<IDevice[]>([]);
  alertRulesSharedCollection = signal<IAlertRule[]>([]);

  protected alertEventService = inject(AlertEventService);
  protected alertEventFormService = inject(AlertEventFormService);
  protected deviceService = inject(DeviceService);
  protected alertRuleService = inject(AlertRuleService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AlertEventFormGroup = this.alertEventFormService.createAlertEventFormGroup();

  compareDevice = (o1: IDevice | null, o2: IDevice | null): boolean => this.deviceService.compareDevice(o1, o2);

  compareAlertRule = (o1: IAlertRule | null, o2: IAlertRule | null): boolean => this.alertRuleService.compareAlertRule(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ alertEvent }) => {
      this.alertEvent = alertEvent;
      if (alertEvent) {
        this.updateForm(alertEvent);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const alertEvent = this.alertEventFormService.getAlertEvent(this.editForm);
    if (alertEvent.id === null) {
      this.subscribeToSaveResponse(this.alertEventService.create(alertEvent));
    } else {
      this.subscribeToSaveResponse(this.alertEventService.update(alertEvent));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IAlertEvent | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(alertEvent: IAlertEvent): void {
    this.alertEvent = alertEvent;
    this.alertEventFormService.resetForm(this.editForm, alertEvent);

    this.devicesSharedCollection.update(devices => this.deviceService.addDeviceToCollectionIfMissing<IDevice>(devices, alertEvent.device));
    this.alertRulesSharedCollection.update(alertRules =>
      this.alertRuleService.addAlertRuleToCollectionIfMissing<IAlertRule>(alertRules, alertEvent.rule),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.deviceService
      .query()
      .pipe(map((res: HttpResponse<IDevice[]>) => res.body ?? []))
      .pipe(map((devices: IDevice[]) => this.deviceService.addDeviceToCollectionIfMissing<IDevice>(devices, this.alertEvent?.device)))
      .subscribe((devices: IDevice[]) => this.devicesSharedCollection.set(devices));

    this.alertRuleService
      .query()
      .pipe(map((res: HttpResponse<IAlertRule[]>) => res.body ?? []))
      .pipe(
        map((alertRules: IAlertRule[]) =>
          this.alertRuleService.addAlertRuleToCollectionIfMissing<IAlertRule>(alertRules, this.alertEvent?.rule),
        ),
      )
      .subscribe((alertRules: IAlertRule[]) => this.alertRulesSharedCollection.set(alertRules));
  }
}
