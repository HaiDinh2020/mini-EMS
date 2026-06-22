import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IAlertRule, NewAlertRule } from '../alert-rule.model';

export type PartialUpdateAlertRule = Partial<IAlertRule> & Pick<IAlertRule, 'id'>;

@Injectable()
export class AlertRulesService {
  readonly alertRulesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly alertRulesResource = httpResource<IAlertRule[]>(() => {
    const params = this.alertRulesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of alertRule that have been fetched. It is updated when the alertRulesResource emits a new value.
   * In case of error while fetching the alertRules, the signal is set to an empty array.
   */
  readonly alertRules = computed(() => (this.alertRulesResource.hasValue() ? this.alertRulesResource.value() : []));
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/alert-rules');
}

@Injectable({ providedIn: 'root' })
export class AlertRuleService extends AlertRulesService {
  protected readonly http = inject(HttpClient);

  create(alertRule: NewAlertRule): Observable<IAlertRule> {
    return this.http.post<IAlertRule>(this.resourceUrl, alertRule);
  }

  update(alertRule: IAlertRule): Observable<IAlertRule> {
    return this.http.put<IAlertRule>(`${this.resourceUrl}/${encodeURIComponent(this.getAlertRuleIdentifier(alertRule))}`, alertRule);
  }

  partialUpdate(alertRule: PartialUpdateAlertRule): Observable<IAlertRule> {
    return this.http.patch<IAlertRule>(`${this.resourceUrl}/${encodeURIComponent(this.getAlertRuleIdentifier(alertRule))}`, alertRule);
  }

  find(id: string): Observable<IAlertRule> {
    return this.http.get<IAlertRule>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<IAlertRule[]>> {
    const options = createRequestOption(req);
    return this.http.get<IAlertRule[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: string): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getAlertRuleIdentifier(alertRule: Pick<IAlertRule, 'id'>): string {
    return alertRule.id;
  }

  compareAlertRule(o1: Pick<IAlertRule, 'id'> | null, o2: Pick<IAlertRule, 'id'> | null): boolean {
    return o1 && o2 ? this.getAlertRuleIdentifier(o1) === this.getAlertRuleIdentifier(o2) : o1 === o2;
  }

  addAlertRuleToCollectionIfMissing<Type extends Pick<IAlertRule, 'id'>>(
    alertRuleCollection: Type[],
    ...alertRulesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const alertRules: Type[] = alertRulesToCheck.filter(isPresent);
    if (alertRules.length > 0) {
      const alertRuleCollectionIdentifiers = alertRuleCollection.map(alertRuleItem => this.getAlertRuleIdentifier(alertRuleItem));
      const alertRulesToAdd = alertRules.filter(alertRuleItem => {
        const alertRuleIdentifier = this.getAlertRuleIdentifier(alertRuleItem);
        if (alertRuleCollectionIdentifiers.includes(alertRuleIdentifier)) {
          return false;
        }
        alertRuleCollectionIdentifiers.push(alertRuleIdentifier);
        return true;
      });
      return [...alertRulesToAdd, ...alertRuleCollection];
    }
    return alertRuleCollection;
  }
}
