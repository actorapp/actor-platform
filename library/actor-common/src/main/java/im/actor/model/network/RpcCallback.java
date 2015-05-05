/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

import im.actor.model.network.parser.Response;

public interface RpcCallback<T extends Response> {
    void onResult(T response);

    void onError(RpcException e);
}
