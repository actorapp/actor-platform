package im.actor.model.network;

/**
 * Created by ex3ndr on 06.02.15.
 */
public class ConnectionFactory {

    private static Factory factory;

    public interface Factory {
        public void createConnection(int connectionId, ConnectionEndpoint endpoint, ConnectionCallback callback, CreateConnectionCallback createCallback);
    }

    public interface CreateConnectionCallback {
        public void onConnectionCreated(Connection connection);

        public void onConnectionCreateError();
    }

    public static void setFactory(Factory factory) {
        ConnectionFactory.factory = factory;
    }

    public static Factory getFactory() {
        return factory;
    }


    public static void createConnection(int connectionId, ConnectionEndpoint endpoint, ConnectionCallback callback, CreateConnectionCallback createCallback) {
        if (factory == null) {
            throw new RuntimeException("Connection factory not inited");
        }

        factory.createConnection(connectionId, endpoint, callback, createCallback);
    }
}
