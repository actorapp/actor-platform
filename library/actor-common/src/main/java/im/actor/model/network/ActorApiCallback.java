/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * API Callback
 */
public interface ActorApiCallback {
    /**
     * Called when Auth Key is invalidated
     *
     * @param authKey invalidated auth key
     */
    @ObjectiveCName("onAuthIdInvalidatedWithAuthKey:")
    void onAuthIdInvalidated(long authKey);

    /**
     * Called when session was (re-)created on server
     */
    @ObjectiveCName("onNewSessionCreated")
    void onNewSessionCreated();

    /**
     * Called when update received
     * @param obj update object
     */
    @ObjectiveCName("onUpdateReceived:")
    void onUpdateReceived(Object obj);
}