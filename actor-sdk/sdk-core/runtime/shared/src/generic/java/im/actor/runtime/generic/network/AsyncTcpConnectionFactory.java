/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.network;

import im.actor.runtime.mtproto.AsyncConnection;
import im.actor.runtime.mtproto.AsyncConnectionFactory;
import im.actor.runtime.mtproto.AsyncConnectionInterface;
import im.actor.runtime.mtproto.ConnectionEndpoint;

public class AsyncTcpConnectionFactory implements AsyncConnectionFactory {

    @Override
    public AsyncConnection createConnection(int connectionId, ConnectionEndpoint endpoint, AsyncConnectionInterface connectionInterface) {
        return new AsyncTcpConnection(connectionId, endpoint, connectionInterface);
    }
}
