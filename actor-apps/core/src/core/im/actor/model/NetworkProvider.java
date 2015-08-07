/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

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
     * @param connectionId    id of connection (useful for logging)
     * @param mtprotoVersion  MTProto version
     * @param apiMajorVersion API Major version
     * @param apiMinorVersion API Minor version
     * @param endpoint        endpoint of connection
     * @param callback        callback for connection
     * @param createCallback  callback for connection creation
     */
    @ObjectiveCName("createConnectionWithId:withMTVersion:withApiMajor:withApiMinor:withEndpoint:withCallback:withCreateCallback:")
    void createConnection(int connectionId,
                          int mtprotoVersion,
                          int apiMajorVersion,
                          int apiMinorVersion,
                          ConnectionEndpoint endpoint,
                          ConnectionCallback callback,
                          CreateConnectionCallback createCallback);
}