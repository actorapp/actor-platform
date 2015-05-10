/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import java.util.List;

import im.actor.model.entity.Notification;

/**
 * Provider for notifications support
 */
public interface NotificationProvider {
    /**
     * Called when message arrive when user is in app (used for play in app sound).
     *
     * @param messenger Messenger object
     */
    void onMessageArriveInApp(Messenger messenger);

    /**
     * On background notification
     *
     * @param messenger          Messenger object
     * @param topNotifications   top 10 notifications
     * @param messagesCount      total messages count
     * @param conversationsCount total conversations count
     * @param silentUpdate       is silent notification update required
     * @param isInApp            is notification performed in application window
     */
    void onNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount,
                        int conversationsCount, boolean silentUpdate, boolean isInApp);

    /**
     * Hide all notifications
     */
    void hideAllNotifications();
}