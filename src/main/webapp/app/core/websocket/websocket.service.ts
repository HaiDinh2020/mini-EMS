import { Injectable, OnDestroy, inject } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';

export interface AlertEventMessage {
  type: string;
  alertEventId: string;
  deviceId: string;
  deviceName: string;
  metricType: string;
  severity: string;
  value: number;
  message: string;
  status: string;
  timestamp: string;
}

export interface DeviceStatusMessage {
  deviceId: string;
  name: string;
  status: string;
  lastCheckedAt: string;
}

@Injectable({ providedIn: 'root' })
export class WebsocketService implements OnDestroy {
  private stompClient: Client | null = null;
  private subscriptions: StompSubscription[] = [];

  readonly alertEvents$ = new Subject<AlertEventMessage>();
  readonly deviceStatus$ = new Subject<DeviceStatusMessage>();

  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly authServerProvider = inject(AuthServerProvider);

  connect(): void {
    if (this.stompClient?.connected) {
      return;
    }
    const token = this.authServerProvider.getToken();
    const wsUrl = this.applicationConfigService.getEndpointFor('websocket/tracker');

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(wsUrl, undefined, { transports: ['websocket'] }),
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        this.subscribeToTopics();
      },
      onStompError: frame => {
        console.warn('STOMP error:', frame.headers['message'], frame.body);
      },
      onWebSocketError: event => {
        console.warn('WebSocket error:', event);
      },
    });
    this.stompClient.activate();
  }

  disconnect(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.subscriptions = [];
    this.stompClient?.deactivate();
    this.stompClient = null;
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  private subscribeToTopics(): void {
    if (!this.stompClient) return;

    const alertSub = this.stompClient.subscribe('/topic/alerts', (msg: IMessage) => {
      try {
        this.alertEvents$.next(JSON.parse(msg.body) as AlertEventMessage);
      } catch {
        // ignore malformed messages
      }
    });

    const statusSub = this.stompClient.subscribe('/topic/device-status', (msg: IMessage) => {
      try {
        this.deviceStatus$.next(JSON.parse(msg.body) as DeviceStatusMessage);
      } catch {
        // ignore malformed messages
      }
    });

    this.subscriptions.push(alertSub, statusSub);
  }
}
