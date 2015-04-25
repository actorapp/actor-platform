package im.actor.model.jvm;

import im.actor.model.NetworkProvider;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.CreateConnectionCallback;

/**
 * Created by ex3ndr on 13.04.15.
 */
public class JavaNetworkProvider implements NetworkProvider {

    @Override
    public void createConnection(final int connectionId,
                                 final int mtprotoVersion,
                                 final int apiMajorVersion,
                                 final int apiMinorVersion,
                                 final ConnectionEndpoint endpoint, final ConnectionCallback callback,
                                 final CreateConnectionCallback createCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    createCallback.onConnectionCreated(
                            new JavaOldTcpConnection(
                                    connectionId,
                                    endpoint,
                                    callback));
//                    createCallback.onConnectionCreated(
//                            new JavaTcpConnection(
//                                    connectionId,
//                                    mtprotoVersion,
//                                    apiMajorVersion,
//                                    apiMinorVersion,
//                                    endpoint,
//                                    callback));
                } catch (Exception e) {
                    e.printStackTrace();
                    createCallback.onConnectionCreateError();
                }
            }
        }.start();
    }
}