package im.actor.model;

import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.CreateConnectionCallback;

/**
 * Provider for network support
 */
public interface NetworkProvider {

    /**
     * Async connection creation
     *
     * @param connectionId   id of connection (useful for logging)
     * @param endpoint       endpoint of connection
     * @param callback       callback for connection
     * @param createCallback callback for connection creation
     */
    public void createConnection(int connectionId,
                                 ConnectionEndpoint endpoint,
                                 ConnectionCallback callback,
                                 CreateConnectionCallback createCallback);
}
