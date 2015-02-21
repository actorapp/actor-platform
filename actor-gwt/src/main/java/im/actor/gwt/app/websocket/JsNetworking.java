package im.actor.gwt.app.websocket;

import im.actor.model.Networking;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.CreateConnectionCallback;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsNetworking implements Networking {
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
