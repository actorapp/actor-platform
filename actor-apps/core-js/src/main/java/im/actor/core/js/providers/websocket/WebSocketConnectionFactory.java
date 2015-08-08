/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers.websocket;

import im.actor.core.network.ConnectionEndpoint;
import im.actor.core.network.connection.AsyncConnection;
import im.actor.core.network.connection.AsyncConnectionFactory;
import im.actor.core.network.connection.AsyncConnectionInterface;

public class WebSocketConnectionFactory implements AsyncConnectionFactory {

    @Override
    public AsyncConnection createConnection(int connectionId, ConnectionEndpoint endpoint, AsyncConnectionInterface connectionInterface) {
        return new WebSocketConnection(endpoint, connectionInterface);
    }
}
