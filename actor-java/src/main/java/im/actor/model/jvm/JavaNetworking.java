package im.actor.model.jvm;

import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.CreateConnectionCallback;
import im.actor.model.Networking;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class JavaNetworking implements Networking {
    @Override
    public void createConnection(final int connectionId,
                                 final ConnectionEndpoint endpoint,
                                 final ConnectionCallback callback,
                                 final CreateConnectionCallback createCallback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    createCallback.onConnectionCreated(new JavaTcpConnection(connectionId, endpoint, callback));
                } catch (Exception e) {
                    e.printStackTrace();
                    createCallback.onConnectionCreateError();
                }
            }
        }.start();
    }
}