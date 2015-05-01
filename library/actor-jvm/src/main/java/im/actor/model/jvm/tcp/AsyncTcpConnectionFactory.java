package im.actor.model.jvm.tcp;

import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.connection.AsyncConnection;
import im.actor.model.network.connection.AsyncConnectionFactory;
import im.actor.model.network.connection.AsyncConnectionInterface;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class AsyncTcpConnectionFactory implements AsyncConnectionFactory {

    @Override
    public AsyncConnection createConnection(int connectionId, ConnectionEndpoint endpoint, AsyncConnectionInterface connectionInterface) {
        return new AsyncTcpConnection(connectionId, endpoint, connectionInterface);
    }
}
