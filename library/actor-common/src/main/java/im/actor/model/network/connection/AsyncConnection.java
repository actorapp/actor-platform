package im.actor.model.network.connection;

import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 29.04.15.
 */
public abstract class AsyncConnection {
    private AsyncConnectionInterface connection;
    private ConnectionEndpoint endpoint;

    public AsyncConnection(ConnectionEndpoint endpoint, AsyncConnectionInterface connection) {
        this.connection = connection;
        this.endpoint = endpoint;
    }

    public abstract void doConnect();

    public abstract void doSend(byte[] data);

    public abstract void doClose();

    protected ConnectionEndpoint getEndpoint() {
        return endpoint;
    }

    protected final void onConnected() {
        connection.onConnected();
    }

    protected final void onReceived(byte[] data) {
        connection.onReceived(data);
    }

    protected final void onClosed() {
        connection.onClosed();
    }
}
