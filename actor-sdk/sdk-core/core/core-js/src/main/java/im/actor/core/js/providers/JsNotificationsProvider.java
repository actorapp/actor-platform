/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers;

import com.google.gwt.media.client.Audio;

import java.util.List;

import im.actor.core.Messenger;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.GroupType;
import im.actor.core.entity.Notification;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.js.entity.JsPeer;
import im.actor.core.js.JsMessenger;
import im.actor.core.js.providers.electron.JsElectronApp;
import im.actor.core.js.providers.notification.JsManagedNotification;
import im.actor.core.js.providers.notification.JsNotification;
import im.actor.core.providers.NotificationProvider;
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
    public void onMessageArriveInApp(Messenger messenger) {
        playSound();
    }

    @Override
    public void onNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount, int conversationsCount) {

        String peerTitle;
        String peerKey = null;
        String peerAvatarUrl = null;
        String contentMessage = "";

        Notification notification = topNotifications.get(0);

        // Peer info
        Peer peer = notification.getPeer();
        if (conversationsCount == 1) {
            Avatar peerAvatar;
            JsPeer jsPeer = JsPeer.create(peer);
            if (peer.getPeerType() == PeerType.PRIVATE) {
                UserVM userVM = messenger.getUser(peer.getPeerId());
                peerTitle = userVM.getName().get();
                peerAvatar = userVM.getAvatar().get();
            } else {
                GroupVM groupVM = messenger.getGroup(peer.getPeerId());
                peerTitle = groupVM.getName().get();
                peerAvatar = groupVM.getAvatar().get();
            }
            if (peerAvatar != null && peerAvatar.getSmallImage() != null) {
                peerAvatarUrl = ((JsMessenger) messenger).getFileUrl(peerAvatar.getSmallImage().getFileReference());
            }
            peerKey = jsPeer.getPeerKey();
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

        boolean isChannel = peer.getPeerType() == PeerType.GROUP && messenger.getGroups().get(peer.getPeerId()).getGroupType() == GroupType.CHANNEL;

        if (conversationsCount == 1) {
            for (int i = 0; i < nCount; i++) {
                Notification n = topNotifications.get(i);
                if (contentMessage.length() > 0) {
                    contentMessage += "\n";
                }
                if (peer.getPeerType() == PeerType.GROUP) {
                    contentMessage += messenger.getUser(notification.getSender()).getName().get() + ": ";
                }
                contentMessage += messenger.getFormatter().formatContentText(n.getSender(),
                        n.getContentDescription().getContentType(),
                        n.getContentDescription().getText(),
                        n.getContentDescription().getRelatedUser(),
                        isChannel);
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
                String senderName = messenger.getUser(n.getSender()).getName().get();
                if (n.getPeer().getPeerType() == PeerType.GROUP) {
                    String groupName = messenger.getGroup(n.getPeer().getPeerId()).getName().get();
                    contentMessage += "[" + groupName + "] " + senderName + ": ";
                } else {
                    contentMessage += senderName + ": ";
                }
                contentMessage += messenger.getFormatter().formatContentText(n.getSender(),
                        n.getContentDescription().getContentType(),
                        n.getContentDescription().getText(),
                        n.getContentDescription().getRelatedUser(),
                        isChannel);
            }

            if (showCounters) {
                contentMessage += "\n+" + (messagesCount - 4) + " new messages in " + conversationsCount + " conversations";
            }
        }

        if (!JsElectronApp.isElectron()) {
            playSound();
        }

        if (!JsNotification.isSupported()) {
            return;
        }
        if (!JsNotification.isGranted()) {
            return;
        }

        JsManagedNotification.show(peerKey, peerTitle, contentMessage, peerAvatarUrl);
    }

    @Override
    public void onUpdateNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount, int conversationsCount) {
        // TODO: Implement
    }

    @Override
    public void hideAllNotifications() {
        if (JsElectronApp.isElectron()) {
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
