/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.connection;

public interface AsyncConnectionInterface {

    void onConnected();

    void onReceived(byte[] data);

    void onClosed();
}