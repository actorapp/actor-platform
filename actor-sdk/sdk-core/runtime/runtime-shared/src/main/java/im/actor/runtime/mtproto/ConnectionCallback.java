/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;

public interface ConnectionCallback {

    @ObjectiveCName("onConnectionRedirectWithHost:withPort:withTimeout:")
    void onConnectionRedirect(String host, int port, int timeout);

    @ObjectiveCName("onMessageWithData:withOffset:withLength:")
    void onMessage(byte[] data, int offset, int len);

    @ObjectiveCName("onConnectionDie")
    void onConnectionDie();
}
