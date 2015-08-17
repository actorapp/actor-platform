/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;

public interface ManagedConnectionCreateCallback {
    @ObjectiveCName("onConnectionCreated:")
    void onConnectionCreated(ManagedConnection connection);

    @ObjectiveCName("onConnectionCreateError:")
    void onConnectionCreateError(ManagedConnection connection);
}
