/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import im.actor.runtime.js.websocket.WebSocketConnectionFactory;
import im.actor.runtime.mtproto.ManagedNetworkProvider;

public class JsNetworkingProvider extends ManagedNetworkProvider {
    public JsNetworkingProvider() {
        super(new WebSocketConnectionFactory());
    }
}
