import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IAlertRule, NewAlertRule } from '../alert-rule.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAlertRule for edit and NewAlertRuleFormGroupInput for create.
 */
type AlertRuleFormGroupInput = IAlertRule | PartialWithRequiredKeyOf<NewAlertRule>;

type AlertRuleFormDefaults = Pick<NewAlertRule, 'id' | 'enabled'>;

type AlertRuleFormGroupContent = {
  id: FormControl<IAlertRule['id'] | NewAlertRule['id']>;
  metricType: FormControl<IAlertRule['metricType']>;
  thresholdWarning: FormControl<IAlertRule['thresholdWarning']>;
  thresholdCritical: FormControl<IAlertRule['thresholdCritical']>;
  enabled: FormControl<IAlertRule['enabled']>;
};

export type AlertRuleFormGroup = FormGroup<AlertRuleFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AlertRuleFormService {
  createAlertRuleFormGroup(alertRule?: AlertRuleFormGroupInput): AlertRuleFormGroup {
    const alertRuleRawValue = {
      ...this.getFormDefaults(),
      ...(alertRule ?? { id: null }),
    };
    return new FormGroup<AlertRuleFormGroupContent>({
      id: new FormControl(
        { value: alertRuleRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      metricType: new FormControl(alertRuleRawValue.metricType, {
        validators: [Validators.required],
      }),
      thresholdWarning: new FormControl(alertRuleRawValue.thresholdWarning, {
        validators: [Validators.required],
      }),
      thresholdCritical: new FormControl(alertRuleRawValue.thresholdCritical, {
        validators: [Validators.required],
      }),
      enabled: new FormControl(alertRuleRawValue.enabled, {
        validators: [Validators.required],
      }),
    });
  }

  getAlertRule(form: AlertRuleFormGroup): IAlertRule | NewAlertRule {
    return form.getRawValue();
  }

  resetForm(form: AlertRuleFormGroup, alertRule: AlertRuleFormGroupInput): void {
    const alertRuleRawValue = { ...this.getFormDefaults(), ...alertRule };
    form.reset({
      ...alertRuleRawValue,
      id: { value: alertRuleRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): AlertRuleFormDefaults {
    return {
      id: null,
      enabled: false,
    };
  }
}
