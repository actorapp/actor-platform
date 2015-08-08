/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers;

import im.actor.core.js.providers.websocket.WebSocketConnectionFactory;
import im.actor.core.network.connection.ManagedNetworkProvider;

public class JsNetworkingProvider extends ManagedNetworkProvider {
    public JsNetworkingProvider() {
        super(new WebSocketConnectionFactory());
    }
}
