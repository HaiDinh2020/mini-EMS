import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ITopologyLink, NewTopologyLink } from '../topology-link.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ITopologyLink for edit and NewTopologyLinkFormGroupInput for create.
 */
type TopologyLinkFormGroupInput = ITopologyLink | PartialWithRequiredKeyOf<NewTopologyLink>;

type TopologyLinkFormDefaults = Pick<NewTopologyLink, 'id'>;

type TopologyLinkFormGroupContent = {
  id: FormControl<ITopologyLink['id'] | NewTopologyLink['id']>;
  linkType: FormControl<ITopologyLink['linkType']>;
  bandwidthMbps: FormControl<ITopologyLink['bandwidthMbps']>;
  status: FormControl<ITopologyLink['status']>;
  sourceDevice: FormControl<ITopologyLink['sourceDevice']>;
  targetDevice: FormControl<ITopologyLink['targetDevice']>;
};

export type TopologyLinkFormGroup = FormGroup<TopologyLinkFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class TopologyLinkFormService {
  createTopologyLinkFormGroup(topologyLink?: TopologyLinkFormGroupInput): TopologyLinkFormGroup {
    const topologyLinkRawValue = {
      ...this.getFormDefaults(),
      ...(topologyLink ?? { id: null }),
    };
    return new FormGroup<TopologyLinkFormGroupContent>({
      id: new FormControl(
        { value: topologyLinkRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      linkType: new FormControl(topologyLinkRawValue.linkType, {
        validators: [Validators.maxLength(255)],
      }),
      bandwidthMbps: new FormControl(topologyLinkRawValue.bandwidthMbps),
      status: new FormControl(topologyLinkRawValue.status, {
        validators: [Validators.required],
      }),
      sourceDevice: new FormControl(topologyLinkRawValue.sourceDevice),
      targetDevice: new FormControl(topologyLinkRawValue.targetDevice),
    });
  }

  getTopologyLink(form: TopologyLinkFormGroup): ITopologyLink | NewTopologyLink {
    return form.getRawValue();
  }

  resetForm(form: TopologyLinkFormGroup, topologyLink: TopologyLinkFormGroupInput): void {
    const topologyLinkRawValue = { ...this.getFormDefaults(), ...topologyLink };
    form.reset({
      ...topologyLinkRawValue,
      id: { value: topologyLinkRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): TopologyLinkFormDefaults {
    return {
      id: null,
    };
  }
}
