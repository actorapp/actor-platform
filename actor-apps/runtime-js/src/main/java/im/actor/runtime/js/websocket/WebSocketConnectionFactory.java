/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.websocket;

import im.actor.runtime.mtproto.AsyncConnection;
import im.actor.runtime.mtproto.AsyncConnectionFactory;
import im.actor.runtime.mtproto.AsyncConnectionInterface;
import im.actor.runtime.mtproto.ConnectionEndpoint;

public class WebSocketConnectionFactory implements AsyncConnectionFactory {

    @Override
    public AsyncConnection createConnection(int connectionId, ConnectionEndpoint endpoint, AsyncConnectionInterface connectionInterface) {
        return new WebSocketConnection(endpoint, connectionInterface);
    }
}
