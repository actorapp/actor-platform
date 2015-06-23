/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.connection;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;

import im.actor.model.NetworkProvider;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.CreateConnectionCallback;

public class ManagedNetworkProvider implements NetworkProvider {

    private final AsyncConnectionFactory factory;
    // Persisting pending connections to avoiding GC
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final ArrayList<ManagedConnection> pendingConnections = new ArrayList<ManagedConnection>();

    @ObjectiveCName("initWithFactory:")
    public ManagedNetworkProvider(AsyncConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public void createConnection(int connectionId, int mtprotoVersion, int apiMajorVersion, int apiMinorVersion, ConnectionEndpoint endpoint, ConnectionCallback callback, final CreateConnectionCallback createCallback) {
        final ManagedConnection managedConnection = new ManagedConnection(connectionId, mtprotoVersion,
                apiMajorVersion, apiMinorVersion, endpoint, callback, new ManagedConnectionCreateCallback() {
            @Override
            public void onConnectionCreated(ManagedConnection connection) {
                createCallback.onConnectionCreated(connection);
                synchronized (pendingConnections) {
                    pendingConnections.remove(connection);
                }
            }

            @Override
            public void onConnectionCreateError(ManagedConnection connection) {
                createCallback.onConnectionCreateError();
                synchronized (pendingConnections) {
                    pendingConnections.remove(connection);
                }
            }
        }, factory);
        synchronized (pendingConnections) {
            pendingConnections.add(managedConnection);
        }
    }
}