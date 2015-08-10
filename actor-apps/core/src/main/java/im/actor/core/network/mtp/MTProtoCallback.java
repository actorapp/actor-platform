/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp;

public interface MTProtoCallback {
    void onRpcResponse(long mid, byte[] content);

    void onUpdate(byte[] content);

    void onAuthKeyInvalidated(long authId);

    void onSessionCreated();

    void onConnectionsCountChanged(int count);
}
