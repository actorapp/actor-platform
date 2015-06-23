/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

public class RpcInternalException extends RpcException {
    public RpcInternalException() {
        super("INTERNAL_ERROR", 500, "Internal server error", true, null);
    }
}
