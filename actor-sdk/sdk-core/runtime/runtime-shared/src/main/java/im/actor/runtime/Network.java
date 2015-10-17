package im.actor.runtime;

import im.actor.runtime.mtproto.ConnectionCallback;
import im.actor.runtime.mtproto.ConnectionEndpoint;
import im.actor.runtime.mtproto.CreateConnectionCallback;

/**
 * Created by ex3ndr on 08.08.15.
 */
public class Network {
    private static NetworkRuntime runtime = new NetworkRuntimeProvider();

    public static void createConnection(int connectionId, int mtprotoVersion, int apiMajorVersion,
                                        int apiMinorVersion, ConnectionEndpoint endpoint,
                                        ConnectionCallback callback, CreateConnectionCallback createCallback) {
        runtime.createConnection(connectionId, mtprotoVersion, apiMajorVersion, apiMinorVersion,
                endpoint, callback, createCallback);
    }
}
