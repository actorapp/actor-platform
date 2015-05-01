package im.actor.model.network.connection;

import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 29.04.15.
 */
public interface AsyncConnectionFactory {
    AsyncConnection createConnection(ConnectionEndpoint endpoint, AsyncConnectionInterface connectionInterface);
}
