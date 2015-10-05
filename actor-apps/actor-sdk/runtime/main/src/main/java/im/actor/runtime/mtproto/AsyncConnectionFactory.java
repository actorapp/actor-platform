/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;

public interface AsyncConnectionFactory {
    @ObjectiveCName("createConnectionWithConnectionId:withEndpoint:withInterface:")
    AsyncConnection createConnection(int connectionId, ConnectionEndpoint endpoint,
                                     AsyncConnectionInterface connectionInterface);
}
