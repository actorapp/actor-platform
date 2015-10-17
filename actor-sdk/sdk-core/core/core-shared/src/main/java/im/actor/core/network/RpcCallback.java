/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.core.network.parser.Response;

public interface RpcCallback<T extends Response> {
    @ObjectiveCName("onResult:")
    void onResult(T response);

    @ObjectiveCName("onError:")
    void onError(RpcException e);
}
