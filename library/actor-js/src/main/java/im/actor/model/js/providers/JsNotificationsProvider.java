/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import com.google.gwt.media.client.Audio;

import java.util.List;

import im.actor.model.Messenger;
import im.actor.model.NotificationProvider;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Notification;
import im.actor.model.entity.PeerType;
import im.actor.model.js.JsMessenger;
import im.actor.model.js.providers.notification.JsNotification;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

public class JsNotificationsProvider implements NotificationProvider {

    private JsNotification currentNotification;

    @Override
    public void onMessageArriveInApp(Messenger messenger) {
        playSound();
    }

    @Override
    public void onNotification(Messenger messenger, List<Notification> topNotifications,
                               int messagesCount, int conversationsCount, boolean silentUpdate,
                               boolean isInApp) {
        if (silentUpdate) {
            return;
        }

        String peerTitle;
        String peerAvatarUrl = null;
        String contentMessage = "";

        Notification notification = topNotifications.get(0);

        // Peer info
        if (conversationsCount == 1) {
            Avatar peerAvatar;
            if (notification.getPeer().getPeerType() == PeerType.PRIVATE) {
                UserVM userVM = messenger.getUsers().get(notification.getPeer().getPeerId());
                peerTitle = userVM.getName().get();
                peerAvatar = userVM.getAvatar().get();
            } else {
                GroupVM groupVM = messenger.getGroups().get(notification.getPeer().getPeerId());
                peerTitle = groupVM.getName().get();
                peerAvatar = groupVM.getAvatar().get();
            }
            if (peerAvatar != null && peerAvatar.getSmallImage() != null) {
                peerAvatarUrl = ((JsMessenger) messenger).getFileUrl(peerAvatar.getSmallImage().getFileReference());
            }
        } else {
            peerTitle = "New messages";
            peerAvatarUrl = "assets/img/notification_icon_512.png";
        }

        // Notification body
        if (conversationsCount == 1) {
            int nCount = Math.min(topNotifications.size(), 5);
            boolean showCounters = false;
            if (topNotifications.size() > 5) {
                nCount--;
                showCounters = true;
            }
            for (int i = 0; i < nCount; i++) {
                Notification n = topNotifications.get(i);
                if (contentMessage.length() > 0) {
                    contentMessage += "\n";
                }
                if (notification.getPeer().getPeerType() == PeerType.GROUP) {
                    contentMessage += messenger.getUsers().get(notification.getSender()).getName().get() + ": ";
                }
                contentMessage += messenger.getFormatter().formatContentText(n.getSender(),
                        n.getContentDescription().getContentType(),
                        n.getContentDescription().getText(),
                        n.getContentDescription().getRelatedUser());
            }

            if (showCounters) {
                contentMessage += "+" + (messagesCount - 4) + " new messages";
            }
        } else {
            contentMessage = "\n" + messagesCount + " new messages from " + conversationsCount + " conversations";
        }

        // Performing notification

        if (currentNotification != null) {
            currentNotification.close();
            currentNotification = null;
        }

        if (!JsNotification.isSupported()) {
            return;
        }
        if (!JsNotification.isGranted()) {
            return;
        }
        currentNotification = JsNotification.create(peerTitle, contentMessage, peerAvatarUrl);

        playSound();
    }

    @Override
    public void hideAllNotifications() {
        if (currentNotification != null) {
            currentNotification.close();
            currentNotification = null;
        }
    }

    private void playSound() {
        Audio audio = Audio.createIfSupported();
        if (audio != null) {
            audio.setSrc("assets/sound/notification.mp3");
            audio.play();
        }
    }
}
