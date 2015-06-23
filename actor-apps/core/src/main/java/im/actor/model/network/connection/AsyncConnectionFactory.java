/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.connection;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.model.network.ConnectionEndpoint;

public interface AsyncConnectionFactory {
    @ObjectiveCName("createConnectionWithConnectionId:withEndpoint:withInterface:")
    AsyncConnection createConnection(int connectionId, ConnectionEndpoint endpoint,
                                     AsyncConnectionInterface connectionInterface);
}
