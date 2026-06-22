import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IAuditLog, NewAuditLog } from '../audit-log.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAuditLog for edit and NewAuditLogFormGroupInput for create.
 */
type AuditLogFormGroupInput = IAuditLog | PartialWithRequiredKeyOf<NewAuditLog>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAuditLog | NewAuditLog> = Omit<T, 'timestamp'> & {
  timestamp?: string | null;
};

type AuditLogFormRawValue = FormValueOf<IAuditLog>;

type NewAuditLogFormRawValue = FormValueOf<NewAuditLog>;

type AuditLogFormDefaults = Pick<NewAuditLog, 'id' | 'timestamp'>;

type AuditLogFormGroupContent = {
  id: FormControl<AuditLogFormRawValue['id'] | NewAuditLog['id']>;
  username: FormControl<AuditLogFormRawValue['username']>;
  action: FormControl<AuditLogFormRawValue['action']>;
  entityName: FormControl<AuditLogFormRawValue['entityName']>;
  entityId: FormControl<AuditLogFormRawValue['entityId']>;
  detail: FormControl<AuditLogFormRawValue['detail']>;
  timestamp: FormControl<AuditLogFormRawValue['timestamp']>;
};

export type AuditLogFormGroup = FormGroup<AuditLogFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AuditLogFormService {
  createAuditLogFormGroup(auditLog?: AuditLogFormGroupInput): AuditLogFormGroup {
    const auditLogRawValue = this.convertAuditLogToAuditLogRawValue({
      ...this.getFormDefaults(),
      ...(auditLog ?? { id: null }),
    });
    return new FormGroup<AuditLogFormGroupContent>({
      id: new FormControl(
        { value: auditLogRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      username: new FormControl(auditLogRawValue.username, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      action: new FormControl(auditLogRawValue.action, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      entityName: new FormControl(auditLogRawValue.entityName, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      entityId: new FormControl(auditLogRawValue.entityId, {
        validators: [Validators.required],
      }),
      detail: new FormControl(auditLogRawValue.detail),
      timestamp: new FormControl(auditLogRawValue.timestamp, {
        validators: [Validators.required],
      }),
    });
  }

  getAuditLog(form: AuditLogFormGroup): IAuditLog | NewAuditLog {
    return this.convertAuditLogRawValueToAuditLog(form.getRawValue());
  }

  resetForm(form: AuditLogFormGroup, auditLog: AuditLogFormGroupInput): void {
    const auditLogRawValue = this.convertAuditLogToAuditLogRawValue({ ...this.getFormDefaults(), ...auditLog });
    form.reset({
      ...auditLogRawValue,
      id: { value: auditLogRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): AuditLogFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      timestamp: currentTime,
    };
  }

  private convertAuditLogRawValueToAuditLog(rawAuditLog: AuditLogFormRawValue | NewAuditLogFormRawValue): IAuditLog | NewAuditLog {
    return {
      ...rawAuditLog,
      timestamp: dayjs(rawAuditLog.timestamp, DATE_TIME_FORMAT),
    };
  }

  private convertAuditLogToAuditLogRawValue(
    auditLog: IAuditLog | (Partial<NewAuditLog> & AuditLogFormDefaults),
  ): AuditLogFormRawValue | PartialWithRequiredKeyOf<NewAuditLogFormRawValue> {
    return {
      ...auditLog,
      timestamp: auditLog.timestamp ? auditLog.timestamp.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
