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
     */
    public void onMessageArriveInApp();

    /**
     * On background notification
     *
     * @param topNotifications   top 10 notifications
     * @param messagesCount      total messages count
     * @param conversationsCount total conversations count
     */
    public void onNotification(List<Notification> topNotifications, int messagesCount,
                               int conversationsCount);

    /**
     * Called on dialogs open (need for hiding all notifications)
     */
    public void onDialogsOpen();

    /**
     * Called on conversation open (need for hiding conversation notifications)
     *
     * @param peer peer of chat
     */
    public void onChatOpen(Peer peer);
}