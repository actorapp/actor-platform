package im.actor.model.js.providers;

import im.actor.model.js.providers.websocket.WebSocketConnectionFactory;
import im.actor.model.network.connection.ManagedNetworkProvider;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class JsNetworkingProvider extends ManagedNetworkProvider {
    public JsNetworkingProvider() {
        super(new WebSocketConnectionFactory());
    }
}
