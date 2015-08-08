/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network;

import im.actor.core.api.ApiVersion;
import im.actor.runtime.actors.ActorRef;
import im.actor.core.network.api.ApiBroker;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.runtime.threading.AtomicIntegerCompat;

/**
 * Actor API Object for connecting to Actor's servers
 */
public class ActorApi {

    public static final int MTPROTO_VERSION = 1;
    public static final int API_MAJOR_VERSION = ApiVersion.VERSION_MAJOR;
    public static final int API_MINOR_VERSION = ApiVersion.VERSION_MINOR;

    private static final AtomicIntegerCompat NEXT_ID = im.actor.runtime.Runtime.createAtomicInt(1);

    private final Endpoints endpoints;
    private final AuthKeyStorage keyStorage;
    private final ActorApiCallback callback;
    private final boolean isEnableLog;
    private final int minDelay;
    private final int maxDelay;
    private final int maxFailureCount;

    private ActorRef apiBroker;

    /**
     * Create API
     *
     * @param endpoints  endpoints for server
     * @param keyStorage storage for authentication keys
     * @param callback   api callback for receiving async events
     */
    public ActorApi(Endpoints endpoints, AuthKeyStorage keyStorage, ActorApiCallback callback,
                    boolean isEnableLog, int minDelay,
                    int maxDelay,
                    int maxFailureCount) {
        this.endpoints = endpoints;
        this.keyStorage = keyStorage;
        this.callback = callback;
        this.isEnableLog = isEnableLog;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.maxFailureCount = maxFailureCount;
        this.apiBroker = ApiBroker.get(endpoints, keyStorage, callback, isEnableLog,
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
