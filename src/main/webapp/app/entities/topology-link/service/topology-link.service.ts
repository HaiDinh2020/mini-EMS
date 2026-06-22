import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ITopologyLink, NewTopologyLink } from '../topology-link.model';

export type PartialUpdateTopologyLink = Partial<ITopologyLink> & Pick<ITopologyLink, 'id'>;

@Injectable()
export class TopologyLinksService {
  readonly topologyLinksParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly topologyLinksResource = httpResource<ITopologyLink[]>(() => {
    const params = this.topologyLinksParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of topologyLink that have been fetched. It is updated when the topologyLinksResource emits a new value.
   * In case of error while fetching the topologyLinks, the signal is set to an empty array.
   */
  readonly topologyLinks = computed(() => (this.topologyLinksResource.hasValue() ? this.topologyLinksResource.value() : []));
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/topology-links');
}

@Injectable({ providedIn: 'root' })
export class TopologyLinkService extends TopologyLinksService {
  protected readonly http = inject(HttpClient);

  create(topologyLink: NewTopologyLink): Observable<ITopologyLink> {
    return this.http.post<ITopologyLink>(this.resourceUrl, topologyLink);
  }

  update(topologyLink: ITopologyLink): Observable<ITopologyLink> {
    return this.http.put<ITopologyLink>(
      `${this.resourceUrl}/${encodeURIComponent(this.getTopologyLinkIdentifier(topologyLink))}`,
      topologyLink,
    );
  }

  partialUpdate(topologyLink: PartialUpdateTopologyLink): Observable<ITopologyLink> {
    return this.http.patch<ITopologyLink>(
      `${this.resourceUrl}/${encodeURIComponent(this.getTopologyLinkIdentifier(topologyLink))}`,
      topologyLink,
    );
  }

  find(id: string): Observable<ITopologyLink> {
    return this.http.get<ITopologyLink>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<ITopologyLink[]>> {
    const options = createRequestOption(req);
    return this.http.get<ITopologyLink[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: string): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getTopologyLinkIdentifier(topologyLink: Pick<ITopologyLink, 'id'>): string {
    return topologyLink.id;
  }

  compareTopologyLink(o1: Pick<ITopologyLink, 'id'> | null, o2: Pick<ITopologyLink, 'id'> | null): boolean {
    return o1 && o2 ? this.getTopologyLinkIdentifier(o1) === this.getTopologyLinkIdentifier(o2) : o1 === o2;
  }

  addTopologyLinkToCollectionIfMissing<Type extends Pick<ITopologyLink, 'id'>>(
    topologyLinkCollection: Type[],
    ...topologyLinksToCheck: (Type | null | undefined)[]
  ): Type[] {
    const topologyLinks: Type[] = topologyLinksToCheck.filter(isPresent);
    if (topologyLinks.length > 0) {
      const topologyLinkCollectionIdentifiers = topologyLinkCollection.map(topologyLinkItem =>
        this.getTopologyLinkIdentifier(topologyLinkItem),
      );
      const topologyLinksToAdd = topologyLinks.filter(topologyLinkItem => {
        const topologyLinkIdentifier = this.getTopologyLinkIdentifier(topologyLinkItem);
        if (topologyLinkCollectionIdentifiers.includes(topologyLinkIdentifier)) {
          return false;
        }
        topologyLinkCollectionIdentifiers.push(topologyLinkIdentifier);
        return true;
      });
      return [...topologyLinksToAdd, ...topologyLinkCollection];
    }
    return topologyLinkCollection;
  }
}
