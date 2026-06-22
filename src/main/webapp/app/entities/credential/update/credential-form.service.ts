import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ICredential, NewCredential } from '../credential.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICredential for edit and NewCredentialFormGroupInput for create.
 */
type CredentialFormGroupInput = ICredential | PartialWithRequiredKeyOf<NewCredential>;

type CredentialFormDefaults = Pick<NewCredential, 'id'>;

type CredentialFormGroupContent = {
  id: FormControl<ICredential['id'] | NewCredential['id']>;
  name: FormControl<ICredential['name']>;
  authType: FormControl<ICredential['authType']>;
  username: FormControl<ICredential['username']>;
  encryptedSecret: FormControl<ICredential['encryptedSecret']>;
};

export type CredentialFormGroup = FormGroup<CredentialFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CredentialFormService {
  createCredentialFormGroup(credential?: CredentialFormGroupInput): CredentialFormGroup {
    const credentialRawValue = {
      ...this.getFormDefaults(),
      ...(credential ?? { id: null }),
    };
    return new FormGroup<CredentialFormGroupContent>({
      id: new FormControl(
        { value: credentialRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(credentialRawValue.name, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      authType: new FormControl(credentialRawValue.authType, {
        validators: [Validators.required],
      }),
      username: new FormControl(credentialRawValue.username, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      encryptedSecret: new FormControl(credentialRawValue.encryptedSecret, {
        validators: [Validators.required],
      }),
    });
  }

  getCredential(form: CredentialFormGroup): ICredential | NewCredential {
    return form.getRawValue();
  }

  resetForm(form: CredentialFormGroup, credential: CredentialFormGroupInput): void {
    const credentialRawValue = { ...this.getFormDefaults(), ...credential };
    form.reset({
      ...credentialRawValue,
      id: { value: credentialRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): CredentialFormDefaults {
    return {
      id: null,
    };
  }
}
