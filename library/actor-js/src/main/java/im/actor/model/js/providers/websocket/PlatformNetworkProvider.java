package im.actor.model.js.providers.websocket;

import im.actor.model.NetworkProvider;
import im.actor.model.network.Connection;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.CreateConnectionCallback;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 29.04.15.
 */
public class PlatformNetworkProvider implements NetworkProvider {

    private final AsyncConnectionFactory factory;
    // Persisting pending connections to avoiding GC
    private final ArrayList<PlatformConnection> pendingConnections = new ArrayList<PlatformConnection>();

    public PlatformNetworkProvider(AsyncConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public void createConnection(int connectionId, int mtprotoVersion, int apiMajorVersion, int apiMinorVersion, ConnectionEndpoint endpoint, ConnectionCallback callback, final CreateConnectionCallback createCallback) {
        final PlatformConnection platformConnection = new PlatformConnection(connectionId, mtprotoVersion,
                apiMajorVersion, apiMinorVersion, endpoint, callback, new PlatformConnectionCreateCallback() {
            @Override
            public void onConnectionCreated(PlatformConnection connection) {
                createCallback.onConnectionCreated(connection);
                synchronized (pendingConnections) {
                    pendingConnections.remove(connection);
                }
            }

            @Override
            public void onConnectionCreateError(PlatformConnection connection) {
                createCallback.onConnectionCreateError();
                synchronized (pendingConnections) {
                    pendingConnections.remove(connection);
                }
            }
        }, factory);
        synchronized (pendingConnections) {
            pendingConnections.add(platformConnection);
        }
    }
}