/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import java.util.List;

import im.actor.model.entity.Notification;
import im.actor.model.entity.Peer;

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
     */
    void onNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount,
                        int conversationsCount);

    /**
     * Called on dialogs open (need for hiding all notifications)
     *
     * @param messenger Messenger object
     */
    void onDialogsOpen(Messenger messenger);

    /**
     * Called on conversation open (need for hiding conversation notifications)
     *
     * @param messenger Messenger object
     * @param peer peer of chat
     */
    void onChatOpen(Messenger messenger, Peer peer);
}