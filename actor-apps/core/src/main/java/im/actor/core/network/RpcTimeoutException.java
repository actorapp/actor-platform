/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network;

public class RpcTimeoutException extends RpcException {
    public RpcTimeoutException() {
        super("TIMEOUT", 500, "Request timeout", true, null);
    }
}
