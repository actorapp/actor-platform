package im.actor.model.js.providers;

import im.actor.model.NotificationProvider;
import im.actor.model.entity.Notification;
import im.actor.model.entity.Peer;

import java.util.List;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsNotificationsProvider implements NotificationProvider {
    @Override
    public void onMessageArriveInApp() {

    }

    @Override
    public void onNotification(List<Notification> topNotifications, int messagesCount, int conversationsCount) {
        Notification notification = topNotifications.get(0);
        showNotification("???", notification.getContentDescription().getText());
    }

    @Override
    public void onDialogsOpen() {

    }

    @Override
    public void onChatOpen(Peer peer) {

    }

    native void showNotification(String title, String message)/*-{
        if (!Notification) {
            return;
        }
        if (Notification.permission !== "granted") {
            Notification.requestPermission();
            return;
        }

        var notification = new Notification(title, {
            body: message
        });
    }-*/;
}
