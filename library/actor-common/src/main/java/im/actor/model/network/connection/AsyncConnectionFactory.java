/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.connection;

import im.actor.model.network.ConnectionEndpoint;

public interface AsyncConnectionFactory {
    AsyncConnection createConnection(int connectionId, ConnectionEndpoint endpoint, AsyncConnectionInterface connectionInterface);
}
