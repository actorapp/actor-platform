/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

public interface ConnectionCallback {

    void onConnectionRedirect(String host, int port, int timeout);

    void onMessage(byte[] data, int offset, int len);

    void onConnectionDie();
}
