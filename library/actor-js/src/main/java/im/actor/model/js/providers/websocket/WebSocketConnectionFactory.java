/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.websocket;

import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.connection.AsyncConnection;
import im.actor.model.network.connection.AsyncConnectionFactory;
import im.actor.model.network.connection.AsyncConnectionInterface;

public class WebSocketConnectionFactory implements AsyncConnectionFactory {

    @Override
    public AsyncConnection createConnection(int connectionId, ConnectionEndpoint endpoint, AsyncConnectionInterface connectionInterface) {
        return new WebSocketConnection(endpoint, connectionInterface);
    }
}
