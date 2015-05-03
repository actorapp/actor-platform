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
import im.actor.model.entity.Peer;
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
    public void onNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount, int conversationsCount) {
        Notification notification = topNotifications.get(0);
        String contentMessage = messenger.getFormatter().formatContentDialogText(notification.getSender(),
                notification.getContentDescription().getContentType(),
                notification.getContentDescription().getText(),
                notification.getContentDescription().getRelatedUser());

        String peerTitle;
        Avatar peerAvatar;
        if (notification.getPeer().getPeerType() == PeerType.PRIVATE) {
            UserVM userVM = messenger.getUsers().get(notification.getPeer().getPeerId());
            peerTitle = userVM.getName().get();
            peerAvatar = userVM.getAvatar().get();
        } else {
            GroupVM groupVM = messenger.getGroups().get(notification.getPeer().getPeerId());
            peerTitle = groupVM.getName().get();
            peerAvatar = groupVM.getAvatar().get();

            contentMessage = messenger.getUsers().get(notification.getSender()).getName().get() + ": " + contentMessage;
        }

        String peerAvatarUrl = null;
        if (peerAvatar != null && peerAvatar.getSmallImage() != null) {
            peerAvatarUrl = ((JsMessenger) messenger).getFileUrl(peerAvatar.getSmallImage().getFileReference());
        }

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
    public void onDialogsOpen(Messenger messenger) {
//        if (currentNotification != null) {
//            currentNotification.close();
//            currentNotification = null;
//        }
    }

    @Override
    public void onChatOpen(Messenger messenger, Peer peer) {

    }

    private void playSound() {
        Audio audio = Audio.createIfSupported();
        if (audio != null) {
            audio.setSrc("assets/sound/notification.mp3");
            audio.play();
        }
    }
}
