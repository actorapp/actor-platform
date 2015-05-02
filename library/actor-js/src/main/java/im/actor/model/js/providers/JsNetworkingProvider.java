/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import im.actor.model.js.providers.websocket.WebSocketConnectionFactory;
import im.actor.model.network.connection.ManagedNetworkProvider;

public class JsNetworkingProvider extends ManagedNetworkProvider {
    public JsNetworkingProvider() {
        super(new WebSocketConnectionFactory());
    }
}
