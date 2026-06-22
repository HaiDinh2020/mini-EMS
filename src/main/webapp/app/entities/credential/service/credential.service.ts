import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ICredential, NewCredential } from '../credential.model';

export type PartialUpdateCredential = Partial<ICredential> & Pick<ICredential, 'id'>;

@Injectable()
export class CredentialsService {
  readonly credentialsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly credentialsResource = httpResource<ICredential[]>(() => {
    const params = this.credentialsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of credential that have been fetched. It is updated when the credentialsResource emits a new value.
   * In case of error while fetching the credentials, the signal is set to an empty array.
   */
  readonly credentials = computed(() => (this.credentialsResource.hasValue() ? this.credentialsResource.value() : []));
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/credentials');
}

@Injectable({ providedIn: 'root' })
export class CredentialService extends CredentialsService {
  protected readonly http = inject(HttpClient);

  create(credential: NewCredential): Observable<ICredential> {
    return this.http.post<ICredential>(this.resourceUrl, credential);
  }

  update(credential: ICredential): Observable<ICredential> {
    return this.http.put<ICredential>(`${this.resourceUrl}/${encodeURIComponent(this.getCredentialIdentifier(credential))}`, credential);
  }

  partialUpdate(credential: PartialUpdateCredential): Observable<ICredential> {
    return this.http.patch<ICredential>(`${this.resourceUrl}/${encodeURIComponent(this.getCredentialIdentifier(credential))}`, credential);
  }

  find(id: string): Observable<ICredential> {
    return this.http.get<ICredential>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<ICredential[]>> {
    const options = createRequestOption(req);
    return this.http.get<ICredential[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: string): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getCredentialIdentifier(credential: Pick<ICredential, 'id'>): string {
    return credential.id;
  }

  compareCredential(o1: Pick<ICredential, 'id'> | null, o2: Pick<ICredential, 'id'> | null): boolean {
    return o1 && o2 ? this.getCredentialIdentifier(o1) === this.getCredentialIdentifier(o2) : o1 === o2;
  }

  addCredentialToCollectionIfMissing<Type extends Pick<ICredential, 'id'>>(
    credentialCollection: Type[],
    ...credentialsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const credentials: Type[] = credentialsToCheck.filter(isPresent);
    if (credentials.length > 0) {
      const credentialCollectionIdentifiers = credentialCollection.map(credentialItem => this.getCredentialIdentifier(credentialItem));
      const credentialsToAdd = credentials.filter(credentialItem => {
        const credentialIdentifier = this.getCredentialIdentifier(credentialItem);
        if (credentialCollectionIdentifiers.includes(credentialIdentifier)) {
          return false;
        }
        credentialCollectionIdentifiers.push(credentialIdentifier);
        return true;
      });
      return [...credentialsToAdd, ...credentialCollection];
    }
    return credentialCollection;
  }
}
