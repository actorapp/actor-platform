/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.connection;

import com.google.j2objc.annotations.ObjectiveCName;

public interface AsyncConnectionInterface {

    @ObjectiveCName("onConnected")
    void onConnected();

    @ObjectiveCName("onReceived:")
    void onReceived(byte[] data);

    @ObjectiveCName("onClosed")
    void onClosed();
}