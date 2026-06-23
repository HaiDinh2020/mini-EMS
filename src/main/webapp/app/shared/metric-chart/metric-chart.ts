import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild,
  inject,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import dayjs from 'dayjs/esm';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

Chart.register(...registerables);

export type MetricChartType = 'CPU' | 'RAM' | 'DISK';
export type TimeRange = '1h' | '6h' | '24h';

interface MetricSampleDTO {
  id: string;
  cpuUsage?: number;
  ramUsage?: number;
  diskUsage?: number;
  collectedAt: string;
}

@Component({
  selector: 'jhi-metric-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './metric-chart.html',
  imports: [FormsModule],
})
export class MetricChartComponent implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  @Input() deviceId!: string;
  @Input() metricType: MetricChartType = 'CPU';
  @Input() warningThreshold?: number;
  @Input() criticalThreshold?: number;

  selectedRange: TimeRange = '1h';

  private chart: Chart | null = null;
  private initialized = false;

  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  ngAfterViewInit(): void {
    this.initialized = true;
    this.createChart();
    this.loadData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.initialized && (changes['deviceId'] || changes['metricType'])) {
      this.loadData();
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  onRangeChange(): void {
    this.loadData();
  }

  private createChart(): void {
    const canvas = this.chartCanvas.nativeElement;
    const colors: Record<MetricChartType, string> = {
      CPU: 'rgba(54, 162, 235, 0.8)',
      RAM: 'rgba(255, 99, 132, 0.8)',
      DISK: 'rgba(75, 192, 192, 0.8)',
    };

    const config: ChartConfiguration = {
      type: 'line',
      data: {
        labels: [],
        datasets: [
          {
            label: `${this.metricType} Usage (%)`,
            data: [],
            borderColor: colors[this.metricType],
            backgroundColor: colors[this.metricType].replace('0.8', '0.1'),
            tension: 0.3,
            fill: true,
            pointRadius: 3,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: true, position: 'top' },
          tooltip: {
            callbacks: {
              label: ctx => {
                const value = ctx.parsed.y;
                return value == null ? '' : `${value.toFixed(1)}%`;
              },
            },
          },
        },
        scales: {
          x: {
            display: true,
            ticks: { maxTicksLimit: 8, maxRotation: 0 },
          },
          y: {
            display: true,
            min: 0,
            max: 100,
            ticks: { callback: v => `${v}%` },
          },
        },
      },
    };

    this.chart = new Chart(canvas, config);
    this.addThresholdAnnotations();
  }

  private addThresholdAnnotations(): void {
    if (!this.chart) return;
    const datasets = this.chart.data.datasets;

    if (this.warningThreshold != null) {
      datasets.push({
        label: `Warning (${this.warningThreshold}%)`,
        data: [],
        borderColor: 'rgba(255, 193, 7, 0.8)',
        borderDash: [5, 5],
        borderWidth: 1.5,
        pointRadius: 0,
        fill: false,
      } as any);
    }
    if (this.criticalThreshold != null) {
      datasets.push({
        label: `Critical (${this.criticalThreshold}%)`,
        data: [],
        borderColor: 'rgba(220, 53, 69, 0.8)',
        borderDash: [5, 5],
        borderWidth: 1.5,
        pointRadius: 0,
        fill: false,
      } as any);
    }
  }

  private loadData(): void {
    if (!this.deviceId || !this.chart) return;

    const now = dayjs();
    const hoursMap: Record<TimeRange, number> = { '1h': 1, '6h': 6, '24h': 24 };
    const from = now.subtract(hoursMap[this.selectedRange], 'hour').toISOString();
    const to = now.toISOString();
    const limit = this.selectedRange === '24h' ? 288 : this.selectedRange === '6h' ? 72 : 50;

    const url = this.applicationConfigService.getEndpointFor(`api/metric-samples/devices/${this.deviceId}/metrics/history`);
    this.http.get<MetricSampleDTO[]>(url, { params: { from, to, limit } }).subscribe({
      next: samples => this.updateChart(samples),
    });
  }

  private updateChart(samples: MetricSampleDTO[]): void {
    if (!this.chart) return;

    const labels = samples.map(s => dayjs(s.collectedAt).format('HH:mm'));
    const values = samples.map(s => this.extractValue(s) ?? 0);

    this.chart.data.labels = labels;
    this.chart.data.datasets[0].data = values;
    this.chart.data.datasets[0].label = `${this.metricType} Usage (%)`;

    // update threshold horizontal lines
    const warnIdx = this.warningThreshold != null ? 1 : -1;
    const critIdx = this.criticalThreshold != null ? (warnIdx > 0 ? 2 : 1) : -1;

    if (warnIdx > 0 && this.chart.data.datasets[warnIdx]) {
      this.chart.data.datasets[warnIdx].data = labels.map(() => this.warningThreshold!);
    }
    if (critIdx > 0 && this.chart.data.datasets[critIdx]) {
      this.chart.data.datasets[critIdx].data = labels.map(() => this.criticalThreshold!);
    }

    this.chart.update();
  }

  private extractValue(sample: MetricSampleDTO): number | undefined {
    switch (this.metricType) {
      case 'CPU':
        return sample.cpuUsage;
      case 'RAM':
        return sample.ramUsage;
      case 'DISK':
        return sample.diskUsage;
    }
  }
}
