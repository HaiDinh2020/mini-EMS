import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { ICredential } from 'app/entities/credential/credential.model';
import { CredentialService } from 'app/entities/credential/service/credential.service';
import { DeviceStatus } from 'app/entities/enumerations/device-status.model';
import { DeviceType } from 'app/entities/enumerations/device-type.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { AlertErrorModel } from 'app/shared/alert/alert-error.model';
import { TranslateDirective } from 'app/shared/language';
import { IDevice } from '../device.model';
import { DeviceService } from '../service/device.service';

import { DeviceFormGroup, DeviceFormService } from './device-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-device-update',
  templateUrl: './device-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class DeviceUpdate implements OnInit {
  readonly isSaving = signal(false);
  device: IDevice | null = null;
  deviceTypeValues = Object.keys(DeviceType);
  deviceStatusValues = Object.keys(DeviceStatus);

  credentialsSharedCollection = signal<ICredential[]>([]);

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected deviceService = inject(DeviceService);
  protected deviceFormService = inject(DeviceFormService);
  protected credentialService = inject(CredentialService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: DeviceFormGroup = this.deviceFormService.createDeviceFormGroup();

  compareCredential = (o1: ICredential | null, o2: ICredential | null): boolean => this.credentialService.compareCredential(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ device }) => {
      this.device = device;
      if (device) {
        this.updateForm(device);
      }

      this.loadRelationshipsOptions();
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(new EventWithContent<AlertErrorModel>('emsApp.error', { ...err, key: `error.file.${err.key}` })),
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const device = this.deviceFormService.getDevice(this.editForm);
    if (device.id === null) {
      this.subscribeToSaveResponse(this.deviceService.create(device));
    } else {
      this.subscribeToSaveResponse(this.deviceService.update(device));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IDevice | null>): void {
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

  protected updateForm(device: IDevice): void {
    this.device = device;
    this.deviceFormService.resetForm(this.editForm, device);

    this.credentialsSharedCollection.update(credentials =>
      this.credentialService.addCredentialToCollectionIfMissing<ICredential>(credentials, device.credential),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.credentialService
      .query()
      .pipe(map((res: HttpResponse<ICredential[]>) => res.body ?? []))
      .pipe(
        map((credentials: ICredential[]) =>
          this.credentialService.addCredentialToCollectionIfMissing<ICredential>(credentials, this.device?.credential),
        ),
      )
      .subscribe((credentials: ICredential[]) => this.credentialsSharedCollection.set(credentials));
  }
}
