package im.actor.model.js.providers.websocket;

import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.connection.AsyncConnection;
import im.actor.model.network.connection.AsyncConnectionFactory;
import im.actor.model.network.connection.AsyncConnectionInterface;

/**
 * Created by ex3ndr on 29.04.15.
 */
public class WebSocketConnectionFactory implements AsyncConnectionFactory {
    @Override
    public AsyncConnection createConnection(ConnectionEndpoint endpoint, AsyncConnectionInterface connectionInterface) {
        return new WebSocketConnection(endpoint, connectionInterface);
    }
}
