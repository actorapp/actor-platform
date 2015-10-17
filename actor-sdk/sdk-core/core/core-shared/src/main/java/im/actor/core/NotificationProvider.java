/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

import im.actor.core.entity.Notification;

/**
 * Provider for notifications support
 */
public interface NotificationProvider {
    /**
     * Called when message arrive when user is in app (used for play in app sound).
     *
     * @param messenger Messenger object
     */
    @ObjectiveCName("onMessageArriveInAppWithMessenger:")
    void onMessageArriveInApp(Messenger messenger);

    /**
     * On background notification
     *
     * @param messenger          Messenger object
     * @param topNotifications   top 10 notifications
     * @param messagesCount      total messages count
     * @param conversationsCount total conversations count
     */
    @ObjectiveCName("onNotificationWithMessenger:withTopNotifications:withMessagesCount:withConversationsCount:")
    void onNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount, int conversationsCount);

    /**
     * On Notification panel update
     *
     * @param messenger          Messenger object
     * @param topNotifications   top 10 notifications
     * @param messagesCount      total messages count
     * @param conversationsCount total conversations count
     */
    @ObjectiveCName("onUpdateNotificationWithMessenger:withTopNotifications:withMessagesCount:withConversationsCount:")
    void onUpdateNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount,
                              int conversationsCount);

    /**
     * Hide all notifications
     */
    @ObjectiveCName("hideAllNotifications")
    void hideAllNotifications();
}