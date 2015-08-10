package im.actor.runtime;

import im.actor.runtime.mtproto.ConnectionCallback;
import im.actor.runtime.mtproto.ConnectionEndpoint;
import im.actor.runtime.mtproto.CreateConnectionCallback;

public class NetworkRuntimeProvider implements NetworkRuntime {
    @Override
    public void createConnection(int connectionId, int mtprotoVersion, int apiMajorVersion, int apiMinorVersion, ConnectionEndpoint endpoint, ConnectionCallback callback, CreateConnectionCallback createCallback) {
        throw new RuntimeException("Dumb");
    }
}
