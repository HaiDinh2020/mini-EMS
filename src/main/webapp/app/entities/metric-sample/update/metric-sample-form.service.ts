import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IMetricSample, NewMetricSample } from '../metric-sample.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IMetricSample for edit and NewMetricSampleFormGroupInput for create.
 */
type MetricSampleFormGroupInput = IMetricSample | PartialWithRequiredKeyOf<NewMetricSample>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IMetricSample | NewMetricSample> = Omit<T, 'collectedAt'> & {
  collectedAt?: string | null;
};

type MetricSampleFormRawValue = FormValueOf<IMetricSample>;

type NewMetricSampleFormRawValue = FormValueOf<NewMetricSample>;

type MetricSampleFormDefaults = Pick<NewMetricSample, 'id' | 'collectedAt'>;

type MetricSampleFormGroupContent = {
  id: FormControl<MetricSampleFormRawValue['id'] | NewMetricSample['id']>;
  cpuUsage: FormControl<MetricSampleFormRawValue['cpuUsage']>;
  ramUsage: FormControl<MetricSampleFormRawValue['ramUsage']>;
  diskUsage: FormControl<MetricSampleFormRawValue['diskUsage']>;
  pingLatencyMs: FormControl<MetricSampleFormRawValue['pingLatencyMs']>;
  collectedAt: FormControl<MetricSampleFormRawValue['collectedAt']>;
  device: FormControl<MetricSampleFormRawValue['device']>;
};

export type MetricSampleFormGroup = FormGroup<MetricSampleFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class MetricSampleFormService {
  createMetricSampleFormGroup(metricSample?: MetricSampleFormGroupInput): MetricSampleFormGroup {
    const metricSampleRawValue = this.convertMetricSampleToMetricSampleRawValue({
      ...this.getFormDefaults(),
      ...(metricSample ?? { id: null }),
    });
    return new FormGroup<MetricSampleFormGroupContent>({
      id: new FormControl(
        { value: metricSampleRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      cpuUsage: new FormControl(metricSampleRawValue.cpuUsage),
      ramUsage: new FormControl(metricSampleRawValue.ramUsage),
      diskUsage: new FormControl(metricSampleRawValue.diskUsage),
      pingLatencyMs: new FormControl(metricSampleRawValue.pingLatencyMs),
      collectedAt: new FormControl(metricSampleRawValue.collectedAt, {
        validators: [Validators.required],
      }),
      device: new FormControl(metricSampleRawValue.device),
    });
  }

  getMetricSample(form: MetricSampleFormGroup): IMetricSample | NewMetricSample {
    return this.convertMetricSampleRawValueToMetricSample(form.getRawValue());
  }

  resetForm(form: MetricSampleFormGroup, metricSample: MetricSampleFormGroupInput): void {
    const metricSampleRawValue = this.convertMetricSampleToMetricSampleRawValue({ ...this.getFormDefaults(), ...metricSample });
    form.reset({
      ...metricSampleRawValue,
      id: { value: metricSampleRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): MetricSampleFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      collectedAt: currentTime,
    };
  }

  private convertMetricSampleRawValueToMetricSample(
    rawMetricSample: MetricSampleFormRawValue | NewMetricSampleFormRawValue,
  ): IMetricSample | NewMetricSample {
    return {
      ...rawMetricSample,
      collectedAt: dayjs(rawMetricSample.collectedAt, DATE_TIME_FORMAT),
    };
  }

  private convertMetricSampleToMetricSampleRawValue(
    metricSample: IMetricSample | (Partial<NewMetricSample> & MetricSampleFormDefaults),
  ): MetricSampleFormRawValue | PartialWithRequiredKeyOf<NewMetricSampleFormRawValue> {
    return {
      ...metricSample,
      collectedAt: metricSample.collectedAt ? metricSample.collectedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
