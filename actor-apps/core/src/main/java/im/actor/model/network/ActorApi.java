/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

import im.actor.model.NetworkProvider;
import im.actor.model.api.ApiVersion;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Environment;
import im.actor.model.network.api.ApiBroker;
import im.actor.model.network.parser.Request;
import im.actor.model.network.parser.Response;
import im.actor.model.util.AtomicIntegerCompat;

/**
 * Actor API Object for connecting to Actor's servers
 */
public class ActorApi {

    public static final int MTPROTO_VERSION = 1;
    public static final int API_MAJOR_VERSION = ApiVersion.VERSION_MAJOR;
    public static final int API_MINOR_VERSION = ApiVersion.VERSION_MINOR;

    private static final AtomicIntegerCompat NEXT_ID = Environment.createAtomicInt(1);

    private final Endpoints endpoints;
    private final AuthKeyStorage keyStorage;
    private final ActorApiCallback callback;
    private final NetworkProvider networkProvider;
    private final boolean isEnableLog;
    private final int minDelay;
    private final int maxDelay;
    private final int maxFailureCount;

    private ActorRef apiBroker;

    /**
     * Create API
     *
     * @param endpoints       endpoints for server
     * @param keyStorage      storage for authentication keys
     * @param callback        api callback for receiving async events
     * @param networkProvider network provider for low level networking
     */
    public ActorApi(Endpoints endpoints, AuthKeyStorage keyStorage, ActorApiCallback callback,
                    NetworkProvider networkProvider, boolean isEnableLog, int minDelay,
                    int maxDelay,
                    int maxFailureCount) {
        this.endpoints = endpoints;
        this.keyStorage = keyStorage;
        this.callback = callback;
        this.networkProvider = networkProvider;
        this.isEnableLog = isEnableLog;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.maxFailureCount = maxFailureCount;
        this.apiBroker = ApiBroker.get(endpoints, keyStorage, callback, networkProvider, isEnableLog,
                NEXT_ID.get(), minDelay, maxDelay, maxFailureCount);
    }

    /**
     * Performing API request
     *
     * @param request  request body
     * @param callback request callback
     * @param <T>      type of response
     */
    public synchronized <T extends Response> void request(Request<T> request, RpcCallback<T> callback) {
        if (request == null) {
            throw new RuntimeException("Request can't be null");
        }

        // Check that request is valid
        request.toByteArray();

        this.apiBroker.send(new ApiBroker.PerformRequest(request, callback));
    }

    /**
     * Notification about network state change
     *
     * @param state current network state if available
     */
    public synchronized void onNetworkChanged(NetworkState state) {
        this.apiBroker.send(new ApiBroker.NetworkChanged(state));
    }

    /**
     * Forcing network connection check
     */
    public synchronized void forceNetworkCheck() {
        this.apiBroker.send(new ApiBroker.ForceNetworkCheck());
    }
}
