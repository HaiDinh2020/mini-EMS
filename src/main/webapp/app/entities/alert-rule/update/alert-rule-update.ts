import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize } from 'rxjs';

import { MetricType } from 'app/entities/enumerations/metric-type.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IAlertRule } from '../alert-rule.model';
import { AlertRuleService } from '../service/alert-rule.service';

import { AlertRuleFormGroup, AlertRuleFormService } from './alert-rule-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-alert-rule-update',
  templateUrl: './alert-rule-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class AlertRuleUpdate implements OnInit {
  readonly isSaving = signal(false);
  alertRule: IAlertRule | null = null;
  metricTypeValues = Object.keys(MetricType);

  protected alertRuleService = inject(AlertRuleService);
  protected alertRuleFormService = inject(AlertRuleFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AlertRuleFormGroup = this.alertRuleFormService.createAlertRuleFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ alertRule }) => {
      this.alertRule = alertRule;
      if (alertRule) {
        this.updateForm(alertRule);
      }
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const alertRule = this.alertRuleFormService.getAlertRule(this.editForm);
    if (alertRule.id === null) {
      this.subscribeToSaveResponse(this.alertRuleService.create(alertRule));
    } else {
      this.subscribeToSaveResponse(this.alertRuleService.update(alertRule));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IAlertRule | null>): void {
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

  protected updateForm(alertRule: IAlertRule): void {
    this.alertRule = alertRule;
    this.alertRuleFormService.resetForm(this.editForm, alertRule);
  }
}
