import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IAlertEvent, NewAlertEvent } from '../alert-event.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAlertEvent for edit and NewAlertEventFormGroupInput for create.
 */
type AlertEventFormGroupInput = IAlertEvent | PartialWithRequiredKeyOf<NewAlertEvent>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAlertEvent | NewAlertEvent> = Omit<T, 'triggeredAt' | 'resolvedAt'> & {
  triggeredAt?: string | null;
  resolvedAt?: string | null;
};

type AlertEventFormRawValue = FormValueOf<IAlertEvent>;

type NewAlertEventFormRawValue = FormValueOf<NewAlertEvent>;

type AlertEventFormDefaults = Pick<NewAlertEvent, 'id' | 'triggeredAt' | 'resolvedAt'>;

type AlertEventFormGroupContent = {
  id: FormControl<AlertEventFormRawValue['id'] | NewAlertEvent['id']>;
  metricType: FormControl<AlertEventFormRawValue['metricType']>;
  value: FormControl<AlertEventFormRawValue['value']>;
  severity: FormControl<AlertEventFormRawValue['severity']>;
  message: FormControl<AlertEventFormRawValue['message']>;
  triggeredAt: FormControl<AlertEventFormRawValue['triggeredAt']>;
  resolvedAt: FormControl<AlertEventFormRawValue['resolvedAt']>;
  status: FormControl<AlertEventFormRawValue['status']>;
  device: FormControl<AlertEventFormRawValue['device']>;
  rule: FormControl<AlertEventFormRawValue['rule']>;
};

export type AlertEventFormGroup = FormGroup<AlertEventFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AlertEventFormService {
  createAlertEventFormGroup(alertEvent?: AlertEventFormGroupInput): AlertEventFormGroup {
    const alertEventRawValue = this.convertAlertEventToAlertEventRawValue({
      ...this.getFormDefaults(),
      ...(alertEvent ?? { id: null }),
    });
    return new FormGroup<AlertEventFormGroupContent>({
      id: new FormControl(
        { value: alertEventRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      metricType: new FormControl(alertEventRawValue.metricType, {
        validators: [Validators.required],
      }),
      value: new FormControl(alertEventRawValue.value, {
        validators: [Validators.required],
      }),
      severity: new FormControl(alertEventRawValue.severity, {
        validators: [Validators.required],
      }),
      message: new FormControl(alertEventRawValue.message, {
        validators: [Validators.required, Validators.maxLength(1000)],
      }),
      triggeredAt: new FormControl(alertEventRawValue.triggeredAt, {
        validators: [Validators.required],
      }),
      resolvedAt: new FormControl(alertEventRawValue.resolvedAt),
      status: new FormControl(alertEventRawValue.status, {
        validators: [Validators.required],
      }),
      device: new FormControl(alertEventRawValue.device),
      rule: new FormControl(alertEventRawValue.rule),
    });
  }

  getAlertEvent(form: AlertEventFormGroup): IAlertEvent | NewAlertEvent {
    return this.convertAlertEventRawValueToAlertEvent(form.getRawValue());
  }

  resetForm(form: AlertEventFormGroup, alertEvent: AlertEventFormGroupInput): void {
    const alertEventRawValue = this.convertAlertEventToAlertEventRawValue({ ...this.getFormDefaults(), ...alertEvent });
    form.reset({
      ...alertEventRawValue,
      id: { value: alertEventRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): AlertEventFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      triggeredAt: currentTime,
      resolvedAt: currentTime,
    };
  }

  private convertAlertEventRawValueToAlertEvent(
    rawAlertEvent: AlertEventFormRawValue | NewAlertEventFormRawValue,
  ): IAlertEvent | NewAlertEvent {
    return {
      ...rawAlertEvent,
      triggeredAt: dayjs(rawAlertEvent.triggeredAt, DATE_TIME_FORMAT),
      resolvedAt: dayjs(rawAlertEvent.resolvedAt, DATE_TIME_FORMAT),
    };
  }

  private convertAlertEventToAlertEventRawValue(
    alertEvent: IAlertEvent | (Partial<NewAlertEvent> & AlertEventFormDefaults),
  ): AlertEventFormRawValue | PartialWithRequiredKeyOf<NewAlertEventFormRawValue> {
    return {
      ...alertEvent,
      triggeredAt: alertEvent.triggeredAt ? alertEvent.triggeredAt.format(DATE_TIME_FORMAT) : undefined,
      resolvedAt: alertEvent.resolvedAt ? alertEvent.resolvedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
