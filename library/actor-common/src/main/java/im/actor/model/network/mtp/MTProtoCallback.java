/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp;

public interface MTProtoCallback {
    void onRpcResponse(long mid, byte[] content);

    void onUpdate(byte[] content);

    void onAuthKeyInvalidated(long authKey);

    void onSessionCreated();
}
