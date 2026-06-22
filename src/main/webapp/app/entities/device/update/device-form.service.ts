import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IDevice, NewDevice } from '../device.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IDevice for edit and NewDeviceFormGroupInput for create.
 */
type DeviceFormGroupInput = IDevice | PartialWithRequiredKeyOf<NewDevice>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IDevice | NewDevice> = Omit<T, 'lastCheckedAt'> & {
  lastCheckedAt?: string | null;
};

type DeviceFormRawValue = FormValueOf<IDevice>;

type NewDeviceFormRawValue = FormValueOf<NewDevice>;

type DeviceFormDefaults = Pick<NewDevice, 'id' | 'lastCheckedAt' | 'monitoringEnabled'>;

type DeviceFormGroupContent = {
  id: FormControl<DeviceFormRawValue['id'] | NewDevice['id']>;
  name: FormControl<DeviceFormRawValue['name']>;
  ipAddress: FormControl<DeviceFormRawValue['ipAddress']>;
  hostname: FormControl<DeviceFormRawValue['hostname']>;
  deviceType: FormControl<DeviceFormRawValue['deviceType']>;
  vendor: FormControl<DeviceFormRawValue['vendor']>;
  model: FormControl<DeviceFormRawValue['model']>;
  sshPort: FormControl<DeviceFormRawValue['sshPort']>;
  sshUsername: FormControl<DeviceFormRawValue['sshUsername']>;
  location: FormControl<DeviceFormRawValue['location']>;
  status: FormControl<DeviceFormRawValue['status']>;
  lastCheckedAt: FormControl<DeviceFormRawValue['lastCheckedAt']>;
  monitoringEnabled: FormControl<DeviceFormRawValue['monitoringEnabled']>;
  description: FormControl<DeviceFormRawValue['description']>;
  credential: FormControl<DeviceFormRawValue['credential']>;
};

export type DeviceFormGroup = FormGroup<DeviceFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class DeviceFormService {
  createDeviceFormGroup(device?: DeviceFormGroupInput): DeviceFormGroup {
    const deviceRawValue = this.convertDeviceToDeviceRawValue({
      ...this.getFormDefaults(),
      ...(device ?? { id: null }),
    });
    return new FormGroup<DeviceFormGroupContent>({
      id: new FormControl(
        { value: deviceRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(deviceRawValue.name, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      ipAddress: new FormControl(deviceRawValue.ipAddress, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      hostname: new FormControl(deviceRawValue.hostname, {
        validators: [Validators.maxLength(255)],
      }),
      deviceType: new FormControl(deviceRawValue.deviceType, {
        validators: [Validators.required],
      }),
      vendor: new FormControl(deviceRawValue.vendor, {
        validators: [Validators.maxLength(255)],
      }),
      model: new FormControl(deviceRawValue.model, {
        validators: [Validators.maxLength(255)],
      }),
      sshPort: new FormControl(deviceRawValue.sshPort),
      sshUsername: new FormControl(deviceRawValue.sshUsername, {
        validators: [Validators.maxLength(255)],
      }),
      location: new FormControl(deviceRawValue.location, {
        validators: [Validators.maxLength(500)],
      }),
      status: new FormControl(deviceRawValue.status, {
        validators: [Validators.required],
      }),
      lastCheckedAt: new FormControl(deviceRawValue.lastCheckedAt),
      monitoringEnabled: new FormControl(deviceRawValue.monitoringEnabled, {
        validators: [Validators.required],
      }),
      description: new FormControl(deviceRawValue.description),
      credential: new FormControl(deviceRawValue.credential),
    });
  }

  getDevice(form: DeviceFormGroup): IDevice | NewDevice {
    return this.convertDeviceRawValueToDevice(form.getRawValue());
  }

  resetForm(form: DeviceFormGroup, device: DeviceFormGroupInput): void {
    const deviceRawValue = this.convertDeviceToDeviceRawValue({ ...this.getFormDefaults(), ...device });
    form.reset({
      ...deviceRawValue,
      id: { value: deviceRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): DeviceFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      lastCheckedAt: currentTime,
      monitoringEnabled: false,
    };
  }

  private convertDeviceRawValueToDevice(rawDevice: DeviceFormRawValue | NewDeviceFormRawValue): IDevice | NewDevice {
    return {
      ...rawDevice,
      lastCheckedAt: dayjs(rawDevice.lastCheckedAt, DATE_TIME_FORMAT),
    };
  }

  private convertDeviceToDeviceRawValue(
    device: IDevice | (Partial<NewDevice> & DeviceFormDefaults),
  ): DeviceFormRawValue | PartialWithRequiredKeyOf<NewDeviceFormRawValue> {
    return {
      ...device,
      lastCheckedAt: device.lastCheckedAt ? device.lastCheckedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
