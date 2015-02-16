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

    public Configuration(Networking networking, ConnectionEndpoint[] endpoints,
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

}
