package im.actor.model;

import java.net.URI;
import java.util.ArrayList;

import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class ConfigurationBuilder {

    private LogCallback log;

    private Networking networking;

    private Threading threading;
    private MainThread mainThread;

    private Storage enginesFactory;

    private MessengerCallback callback;

    private ArrayList<ConnectionEndpoint> endpoints = new ArrayList<ConnectionEndpoint>();

    private boolean isMessagesPersisting;

    public ConfigurationBuilder setLog(LogCallback log) {
        this.log = log;
        return this;
    }

    public ConfigurationBuilder setNetworking(Networking networking) {
        this.networking = networking;
        return this;
    }

    public ConfigurationBuilder setThreading(Threading threading) {
        this.threading = threading;
        return this;
    }

    public ConfigurationBuilder setStorage(Storage storage) {
        this.enginesFactory = storage;
        return this;
    }

    public ConfigurationBuilder setCallback(MessengerCallback callback) {
        this.callback = callback;
        return this;
    }

    public ConfigurationBuilder addEndpoint(String url) {
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

    public ConfigurationBuilder setMessagesPersisting(boolean isMessagesPersisting) {
        this.isMessagesPersisting = isMessagesPersisting;
        return this;
    }

    public ConfigurationBuilder setMainThread(MainThread mainThread) {
        this.mainThread = mainThread;
        return this;
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
