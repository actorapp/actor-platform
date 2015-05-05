/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

/**
 * API Callback
 */
public interface ActorApiCallback {
    /**
     * Called when Auth Key is invalidated
     *
     * @param authKey invalidated auth key
     */
    void onAuthIdInvalidated(long authKey);

    /**
     * Called when session was (re-)created on server
     */
    void onNewSessionCreated();

    /**
     * Called when update received
     * @param obj update object
     */
    void onUpdateReceived(Object obj);
}