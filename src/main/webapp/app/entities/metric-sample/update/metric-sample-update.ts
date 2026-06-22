import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IDevice } from 'app/entities/device/device.model';
import { DeviceService } from 'app/entities/device/service/device.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IMetricSample } from '../metric-sample.model';
import { MetricSampleService } from '../service/metric-sample.service';

import { MetricSampleFormGroup, MetricSampleFormService } from './metric-sample-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-metric-sample-update',
  templateUrl: './metric-sample-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class MetricSampleUpdate implements OnInit {
  readonly isSaving = signal(false);
  metricSample: IMetricSample | null = null;

  devicesSharedCollection = signal<IDevice[]>([]);

  protected metricSampleService = inject(MetricSampleService);
  protected metricSampleFormService = inject(MetricSampleFormService);
  protected deviceService = inject(DeviceService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: MetricSampleFormGroup = this.metricSampleFormService.createMetricSampleFormGroup();

  compareDevice = (o1: IDevice | null, o2: IDevice | null): boolean => this.deviceService.compareDevice(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ metricSample }) => {
      this.metricSample = metricSample;
      if (metricSample) {
        this.updateForm(metricSample);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const metricSample = this.metricSampleFormService.getMetricSample(this.editForm);
    if (metricSample.id === null) {
      this.subscribeToSaveResponse(this.metricSampleService.create(metricSample));
    } else {
      this.subscribeToSaveResponse(this.metricSampleService.update(metricSample));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IMetricSample | null>): void {
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

  protected updateForm(metricSample: IMetricSample): void {
    this.metricSample = metricSample;
    this.metricSampleFormService.resetForm(this.editForm, metricSample);

    this.devicesSharedCollection.update(devices =>
      this.deviceService.addDeviceToCollectionIfMissing<IDevice>(devices, metricSample.device),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.deviceService
      .query()
      .pipe(map((res: HttpResponse<IDevice[]>) => res.body ?? []))
      .pipe(map((devices: IDevice[]) => this.deviceService.addDeviceToCollectionIfMissing<IDevice>(devices, this.metricSample?.device)))
      .subscribe((devices: IDevice[]) => this.devicesSharedCollection.set(devices));
  }
}
