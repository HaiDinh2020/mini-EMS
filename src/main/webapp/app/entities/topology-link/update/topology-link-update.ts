import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IDevice } from 'app/entities/device/device.model';
import { DeviceService } from 'app/entities/device/service/device.service';
import { LinkStatus } from 'app/entities/enumerations/link-status.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { TopologyLinkService } from '../service/topology-link.service';
import { ITopologyLink } from '../topology-link.model';

import { TopologyLinkFormGroup, TopologyLinkFormService } from './topology-link-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-topology-link-update',
  templateUrl: './topology-link-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class TopologyLinkUpdate implements OnInit {
  readonly isSaving = signal(false);
  topologyLink: ITopologyLink | null = null;
  linkStatusValues = Object.keys(LinkStatus);

  devicesSharedCollection = signal<IDevice[]>([]);

  protected topologyLinkService = inject(TopologyLinkService);
  protected topologyLinkFormService = inject(TopologyLinkFormService);
  protected deviceService = inject(DeviceService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: TopologyLinkFormGroup = this.topologyLinkFormService.createTopologyLinkFormGroup();

  compareDevice = (o1: IDevice | null, o2: IDevice | null): boolean => this.deviceService.compareDevice(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ topologyLink }) => {
      this.topologyLink = topologyLink;
      if (topologyLink) {
        this.updateForm(topologyLink);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const topologyLink = this.topologyLinkFormService.getTopologyLink(this.editForm);
    if (topologyLink.id === null) {
      this.subscribeToSaveResponse(this.topologyLinkService.create(topologyLink));
    } else {
      this.subscribeToSaveResponse(this.topologyLinkService.update(topologyLink));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ITopologyLink | null>): void {
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

  protected updateForm(topologyLink: ITopologyLink): void {
    this.topologyLink = topologyLink;
    this.topologyLinkFormService.resetForm(this.editForm, topologyLink);

    this.devicesSharedCollection.update(devices =>
      this.deviceService.addDeviceToCollectionIfMissing<IDevice>(devices, topologyLink.sourceDevice, topologyLink.targetDevice),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.deviceService
      .query()
      .pipe(map((res: HttpResponse<IDevice[]>) => res.body ?? []))
      .pipe(
        map((devices: IDevice[]) =>
          this.deviceService.addDeviceToCollectionIfMissing<IDevice>(
            devices,
            this.topologyLink?.sourceDevice,
            this.topologyLink?.targetDevice,
          ),
        ),
      )
      .subscribe((devices: IDevice[]) => this.devicesSharedCollection.set(devices));
  }
}
