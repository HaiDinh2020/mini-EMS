package com.vht.ems.config;

/**
 * WebSocket security is handled by the main SecurityConfiguration:
 * - /websocket/** endpoint requires authentication (JWT bearer token in handshake).
 * - Topic subscriptions (/topic/**) are available to all authenticated users.
 */
public final class WebsocketSecurityConfiguration {

    private WebsocketSecurityConfiguration() {}
}
