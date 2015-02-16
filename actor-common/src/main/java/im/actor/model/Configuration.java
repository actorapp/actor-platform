package im.actor.model;

import java.net.URI;
import java.util.ArrayList;

import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Configuration {

    private final Networking networking;

    private final ConnectionEndpoint[] endpoints;

    private final Threading threading;
    private final MainThread mainThread;

    private final Storage storage;

    private final MessengerCallback callback;

    private final LogCallback log;

    private Configuration(Networking networking, ConnectionEndpoint[] endpoints,
                          Threading threading, MainThread mainThread, Storage storage,
                          MessengerCallback callback, LogCallback log) {
        this.networking = networking;
        this.endpoints = endpoints;
        this.threading = threading;
        this.mainThread = mainThread;
        this.storage = storage;
        this.callback = callback;
        this.log = log;
    }

    public Networking getNetworking() {
        return networking;
    }

    public ConnectionEndpoint[] getEndpoints() {
        return endpoints;
    }

    public Threading getThreading() {
        return threading;
    }

    public MainThread getMainThread() {
        return mainThread;
    }

    public Storage getStorage() {
        return storage;
    }

    public MessengerCallback getCallback() {
        return callback;
    }

    public LogCallback getLog() {
        return log;
    }

    public static class Builder {

        private LogCallback log;

        private Networking networking;

        private Threading threading;
        private MainThread mainThread;

        private Storage enginesFactory;

        private MessengerCallback callback;

        private ArrayList<ConnectionEndpoint> endpoints = new ArrayList<ConnectionEndpoint>();

        private boolean isMessagesPersisting;

        public Builder setLog(LogCallback log) {
            this.log = log;
            return this;
        }

        public Builder setNetworking(Networking networking) {
            this.networking = networking;
            return this;
        }

        public Builder setThreading(Threading threading) {
            this.threading = threading;
            return this;
        }

        public Builder setStorage(Storage storage) {
            this.enginesFactory = storage;
            return this;
        }

        public void setCallback(MessengerCallback callback) {
            this.callback = callback;
        }

        public Builder addEndpoint(String url) {
            URI uri = URI.create(url);
            if (uri.getScheme().toLowerCase().equals("ssl") || uri.getScheme().toLowerCase().equals("tls")) {
                int port = uri.getPort() > 0 ? uri.getPort() : 443;
                String host = uri.getHost();
                endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.TCP_TLS));
            } else if (uri.getScheme().toLowerCase().equals("tcp")) {
                int port = uri.getPort() > 0 ? uri.getPort() : 80;
                String host = uri.getHost();
                endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.TCP));
            } else if (uri.getScheme().toLowerCase().equals("ws")) {
                int port = uri.getPort() > 0 ? uri.getPort() : 80;
                String host = uri.getHost();
                endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.WS));
            } else if (uri.getScheme().toLowerCase().equals("wss")) {
                int port = uri.getPort() > 0 ? uri.getPort() : 443;
                String host = uri.getHost();
                endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.WS_TLS));
            } else {
                throw new RuntimeException("Unknown scheme type: " + uri.getScheme());
            }
            return this;
        }

        public Builder setMessagesPersisting(boolean isMessagesPersisting) {
            this.isMessagesPersisting = isMessagesPersisting;
            return this;
        }

        public void setMainThread(MainThread mainThread) {
            this.mainThread = mainThread;
        }

        public Configuration build() {
            if (networking == null) {
                throw new RuntimeException("Networking is not set");
            }
            if (threading == null) {
                throw new RuntimeException("Threading is not set");
            }
            if (mainThread == null) {
                throw new RuntimeException("Main Thread is not set");
            }
            if (callback == null) {
                throw new RuntimeException("Callback is not set");
            }
            if (enginesFactory == null) {
                throw new RuntimeException("Storage not set");
            }
            if (endpoints.size() == 0) {
                throw new RuntimeException("Endpoints not set");
            }
            return new Configuration(networking, endpoints.toArray(new ConnectionEndpoint[endpoints.size()]),
                    threading, mainThread, enginesFactory, callback, log);
        }
    }
}
