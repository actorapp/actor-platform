package im.actor.model;

import java.util.List;

import im.actor.model.entity.Notification;
import im.actor.model.entity.Peer;

/**
 * Created by ex3ndr on 01.03.15.
 */
public interface NotificationProvider {
    public void onMessageArriveInApp();

    public void onNotification(List<Notification> topNotifications, int messagesCount,
                               int conversationsCount);

    public void onDialogsOpen();

    public void onChatOpen(Peer peer);
}