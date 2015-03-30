package im.actor.model.js.providers;

import im.actor.model.js.providers.websocket.WebSocketConnection;
import im.actor.model.NetworkProvider;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.CreateConnectionCallback;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsNetworkProvider implements NetworkProvider {
    
    @Override
    public void createConnection(int connectionId, ConnectionEndpoint endpoint,
                                 ConnectionCallback callback, CreateConnectionCallback createCallback) {
        String url;
        if (endpoint.getType() == ConnectionEndpoint.Type.WS) {
            url = "ws://" + endpoint.getHost() + ":" + endpoint.getPort() + "/";
        } else if (endpoint.getType() == ConnectionEndpoint.Type.WS_TLS) {
            url = "wss://" + endpoint.getHost() + ":" + endpoint.getPort() + "/";
        } else {
            createCallback.onConnectionCreateError();
            return;
        }

        new WebSocketConnection(url, callback, createCallback);
    }
}
