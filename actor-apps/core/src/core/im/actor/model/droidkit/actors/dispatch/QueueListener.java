/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.actors.dispatch;

/**
 * Listener for monitoring queue changes in dispatchers
 */
public interface QueueListener {
    void onQueueChanged();
}