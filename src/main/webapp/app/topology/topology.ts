import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  inject,
} from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import { DataSet } from 'vis-data';
import { Network } from 'vis-network';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { WebsocketService } from 'app/core/websocket/websocket.service';
import { TopologyService, TopologyNode, TopologyEdge } from './topology.service';

const STATUS_COLORS: Record<string, string> = {
  ONLINE: '#28a745',
  OFFLINE: '#dc3545',
  UNKNOWN: '#6c757d',
};

@Component({
  selector: 'jhi-topology',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './topology.html',
  styleUrl: './topology.scss',
  imports: [FontAwesomeModule],
})
export default class TopologyComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('networkContainer', { static: true }) networkContainer!: ElementRef<HTMLDivElement>;

  loading = true;
  error = false;

  private network: Network | null = null;
  private nodes!: DataSet<{ id: string; label: string; color: string; title: string }>;
  private edges!: DataSet<{ id: string; from: string; to: string; label: string; title: string }>;
  private wsSub?: Subscription;

  private readonly topologyService = inject(TopologyService);
  private readonly wsService = inject(WebsocketService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    this.wsService.connect();
  }

  ngAfterViewInit(): void {
    this.nodes = new DataSet([]);
    this.edges = new DataSet([]);
    this.initNetwork();
    this.loadData();
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
    this.network?.destroy();
    this.network = null;
  }

  private initNetwork(): void {
    const options = {
      nodes: {
        shape: 'dot',
        size: 20,
        font: { size: 14, face: 'Arial' },
        borderWidth: 2,
      },
      edges: {
        width: 2,
        font: { size: 11, align: 'middle' },
        arrows: { to: { enabled: true, scaleFactor: 0.8 } },
        color: { color: '#848484', highlight: '#007bff' },
      },
      physics: {
        stabilization: { iterations: 150 },
        barnesHut: { gravitationalConstant: -6000, springConstant: 0.04 },
      },
      interaction: { hover: true, tooltipDelay: 200 },
    };

    this.network = new Network(this.networkContainer.nativeElement, { nodes: this.nodes, edges: this.edges }, options);

    this.network.on('doubleClick', ({ nodes: clickedNodes }) => {
      if (clickedNodes?.length) {
        this.router.navigate(['/device', clickedNodes[0], 'view']);
      }
    });
  }

  private loadData(): void {
    forkJoin({
      devices: this.topologyService.getDevices(),
      links: this.topologyService.getLinks(),
    }).subscribe({
      next: ({ devices, links }) => {
        this.nodes.add(devices.map(d => this.toVisNode(d)));
        this.edges.add(links.filter(l => l.sourceId && l.targetId).map(l => this.toVisEdge(l)));
        this.loading = false;
        this.cdr.markForCheck();
        this.subscribeToWs(devices);
      },
      error: () => {
        this.loading = false;
        this.error = true;
        this.cdr.markForCheck();
      },
    });
  }

  private subscribeToWs(devices: TopologyNode[]): void {
    this.wsSub = this.wsService.deviceStatus$.subscribe(msg => {
      const existing = devices.find(d => d.id === msg.deviceId);
      if (existing) {
        existing.status = msg.status;
        this.nodes.update({ id: msg.deviceId, color: this.statusColor(msg.status) });
      }
    });
  }

  private toVisNode(d: TopologyNode): { id: string; label: string; color: string; title: string } {
    return {
      id: d.id,
      label: d.name,
      color: this.statusColor(d.status),
      title: `${d.name}\nIP: ${d.ipAddress}\nType: ${d.deviceType}\nStatus: ${d.status}`,
    };
  }

  private toVisEdge(l: TopologyEdge): { id: string; from: string; to: string; label: string; title: string } {
    const bw = l.bandwidthMbps ? `${l.bandwidthMbps >= 1000 ? l.bandwidthMbps / 1000 + ' Gbps' : l.bandwidthMbps + ' Mbps'}` : '';
    return {
      id: l.id,
      from: l.sourceId,
      to: l.targetId,
      label: l.linkType ?? '',
      title: bw,
    };
  }

  private statusColor(status: string): string {
    return STATUS_COLORS[status] ?? STATUS_COLORS['UNKNOWN'];
  }
}
