/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers;

import com.google.gwt.media.client.Audio;

import java.util.List;

import im.actor.core.NotificationProvider;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.Notification;
import im.actor.core.entity.PeerType;
import im.actor.core.js.JsMessenger;
import im.actor.core.js.providers.electron.JsElectronApp;
import im.actor.core.js.providers.notification.JsManagedNotification;
import im.actor.core.js.providers.notification.JsNotification;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;

public class JsNotificationsProvider implements NotificationProvider {

    // private JsNotification currentNotification;

    private Audio inappSound;

    public JsNotificationsProvider() {
        inappSound = Audio.createIfSupported();
        if (inappSound != null) {
            inappSound.setSrc("assets/sound/notification.mp3");
        }
    }

    @Override
    public void onMessageArriveInApp(ModuleContext messenger) {
        playSound();
    }

    @Override
    public void onNotification(ModuleContext messenger, List<Notification> topNotifications,
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
                UserVM userVM = messenger.getUsersModule().getUsers().get(notification.getPeer().getPeerId());
                peerTitle = userVM.getName().get();
                peerAvatar = userVM.getAvatar().get();
            } else {
                GroupVM groupVM = messenger.getGroupsModule().getGroupsCollection().get(notification.getPeer().getPeerId());
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

        int nCount = Math.min(topNotifications.size(), 5);
        boolean showCounters = false;
        if (topNotifications.size() > 5) {
            nCount--;
            showCounters = true;
        }

        if (conversationsCount == 1) {
            for (int i = 0; i < nCount; i++) {
                Notification n = topNotifications.get(i);
                if (contentMessage.length() > 0) {
                    contentMessage += "\n";
                }
                if (notification.getPeer().getPeerType() == PeerType.GROUP) {
                    contentMessage += messenger.getUsersModule().getUsers().get(notification.getSender()).getName().get() + ": ";
                }
                contentMessage += messenger.getI18nModule().formatContentText(n.getSender(),
                        n.getContentDescription().getContentType(),
                        n.getContentDescription().getText(),
                        n.getContentDescription().getRelatedUser());
            }

            if (showCounters) {
                contentMessage += "\n+" + (messagesCount - 4) + " new messages";
            }
        } else {
            for (int i = 0; i < nCount; i++) {
                Notification n = topNotifications.get(i);
                if (contentMessage.length() > 0) {
                    contentMessage += "\n";
                }
                String senderName = messenger.getUsersModule().getUsers().get(n.getSender()).getName().get();
                if (n.getPeer().getPeerType() == PeerType.GROUP) {
                    String groupName = messenger.getGroupsModule().getGroupsCollection().get(n.getPeer().getPeerId()).getName().get();
                    contentMessage += "[" + groupName + "] " + senderName + ": ";
                } else {
                    contentMessage += senderName + ": ";
                }
                contentMessage += messenger.getI18nModule().formatContentText(n.getSender(),
                        n.getContentDescription().getContentType(),
                        n.getContentDescription().getText(),
                        n.getContentDescription().getRelatedUser());
            }

            if (showCounters) {
                contentMessage += "\n+" + (messagesCount - 4) + " new messages in " + conversationsCount + " conversations";
            }
        }

        if (JsElectronApp.isSupported()) {
            JsElectronApp.showNewMessages();
        }

        playSound();

        if (!JsNotification.isSupported()) {
            return;
        }
        if (!JsNotification.isGranted()) {
            return;
        }

        JsManagedNotification.show(peerTitle, contentMessage, peerAvatarUrl);
    }

    @Override
    public void hideAllNotifications() {
        if (JsElectronApp.isSupported()) {
            JsElectronApp.hideNewMessages();
        }
    }

    private void playSound() {
        if (inappSound != null) {
            inappSound.pause();
            inappSound.setCurrentTime(0);
            inappSound.play();
        }
    }
}
