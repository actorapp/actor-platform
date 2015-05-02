/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import java.util.List;

import im.actor.model.Messenger;
import im.actor.model.NotificationProvider;
import im.actor.model.entity.Notification;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;

public class JsNotificationsProvider implements NotificationProvider {
    @Override
    public void onMessageArriveInApp(Messenger messenger) {

    }

    @Override
    public void onNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount, int conversationsCount) {
        Notification notification = topNotifications.get(0);
        String contentMessage = messenger.getFormatter().formatContentDialogText(notification.getSender(),
                notification.getContentDescription().getContentType(),
                notification.getContentDescription().getText(),
                notification.getContentDescription().getRelatedUser());

        String peerTitle;
        if (notification.getPeer().getPeerType() == PeerType.PRIVATE) {
            peerTitle = messenger.getUsers().get(notification.getPeer().getPeerId()).getName().get();
        } else {
            peerTitle = messenger.getGroups().get(notification.getPeer().getPeerId()).getName().get();

            contentMessage = messenger.getUsers().get(notification.getPeer().getPeerId()).getName().get() + ": " + contentMessage;
        }

        showNotification(peerTitle, contentMessage);
    }

    @Override
    public void onDialogsOpen(Messenger messenger) {

    }

    @Override
    public void onChatOpen(Messenger messenger, Peer peer) {

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
