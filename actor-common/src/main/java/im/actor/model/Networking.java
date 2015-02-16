package im.actor.model;

import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.CreateConnectionCallback;

/**
 * Created by ex3ndr on 16.02.15.
 */
public interface Networking {
    public void createConnection(int connectionId,
                                 ConnectionEndpoint endpoint,
                                 ConnectionCallback callback,
                                 CreateConnectionCallback createCallback);
}
