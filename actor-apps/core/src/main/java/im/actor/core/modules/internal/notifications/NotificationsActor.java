/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.notifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.Notification;
import im.actor.core.entity.PeerEntity;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.notifications.entity.PendingNotification;
import im.actor.core.modules.internal.notifications.entity.PendingStorage;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.runtime.storage.SyncKeyValue;

public class NotificationsActor extends ModuleActor {

    private static final int MAX_NOTIFICATION_COUNT = 10;

    private SyncKeyValue storage;
    private PendingStorage pendingStorage;

    private PeerEntity visiblePeer;
    private boolean isAppVisible = false;
    private boolean isDialogsVisible = false;

    private boolean isNotificationsPaused = false;
    private HashSet<PeerEntity> notificationsDuringPause = new HashSet<PeerEntity>();

    public NotificationsActor(ModuleContext messenger) {
        super(messenger);
        this.storage = messenger.getNotificationsModule().getNotificationsStorage();
    }

    @Override
    public void preStart() {
        pendingStorage = new PendingStorage();
        byte[] storage = this.storage.get(0);
        if (storage != null) {
            try {
                pendingStorage = PendingStorage.fromBytes(storage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<PendingNotification> getNotifications() {
        return pendingStorage.getNotifications();
    }

    public void onNewMessage(PeerEntity peer, int sender, long date, ContentDescription description,
                             boolean hasCurrentUserMention) {

        boolean isPeerEnabled = context().getSettingsModule().isNotificationsEnabled(peer);
        boolean isEnabled = (context().getSettingsModule().isNotificationsEnabled() && isPeerEnabled) || hasCurrentUserMention;
        boolean isInAppEnabled = context().getSettingsModule().isInAppEnabled();
        boolean isConversationTonesEnabled = context().getSettingsModule().isConversationTonesEnabled();

        if (!isEnabled && (!(isInAppEnabled && peer.equals(visiblePeer)))) {
            return;
        }

        List<PendingNotification> allPending = getNotifications();
        allPending.add(new PendingNotification(peer, sender, date, description));
        saveStorage();

        if (isNotificationsPaused) {
            notificationsDuringPause.add(peer);
            return;
        }

        if (config().getNotificationProvider() != null) {
            if (isAppVisible) {
                if (visiblePeer != null && visiblePeer.equals(peer)) {
                    if (isConversationTonesEnabled) {
                        config().getNotificationProvider().onMessageArriveInApp(context());
                    }
                } else if (isDialogsVisible) {
                    if (isConversationTonesEnabled) {
                        config().getNotificationProvider().onMessageArriveInApp(context());
                    }
                } else if (isInAppEnabled) {
                    performNotification(false);
                }
            } else {
                performNotification(false);
            }
        }
    }

    public void onMessagesRead(PeerEntity peer, long fromDate) {
        boolean isChanged = false;
        List<PendingNotification> notifications = pendingStorage.getNotifications();
        for (PendingNotification p : notifications.toArray(new PendingNotification[notifications.size()])) {
            if (p.getPeer().equals(peer) && p.getDate() <= fromDate) {
                pendingStorage.getNotifications().remove(p);
                isChanged = true;
            }
        }

        if (isChanged) {
            saveStorage();
            performNotification(true);
        }
    }

    public void onNotificationsPaused() {
        notificationsDuringPause.clear();
        isNotificationsPaused = true;
    }

    public void onNotificationsResumed() {
        isNotificationsPaused = false;
        if (notificationsDuringPause.size() > 0) {
            if (config().getNotificationProvider() != null) {
                if (visiblePeer != null && notificationsDuringPause.contains(visiblePeer)) {
                    if (context().getSettingsModule().isConversationTonesEnabled()) {
                        config().getNotificationProvider().onMessageArriveInApp(context());
                    }
                } else if (isDialogsVisible) {
                    if (context().getSettingsModule().isConversationTonesEnabled()) {
                        config().getNotificationProvider().onMessageArriveInApp(context());
                    }
                } else if (isAppVisible) {
                    if (context().getSettingsModule().isInAppEnabled()) {
                        performNotification(false);
                    }
                } else {
                    performNotification(false);
                }
            }
        }
        notificationsDuringPause.clear();
    }

    public void onConversationVisible(PeerEntity peer) {
        this.visiblePeer = peer;
        performNotification(true);
    }

    public void onConversationHidden(PeerEntity peer) {
        if (visiblePeer != null && visiblePeer.equals(peer)) {
            this.visiblePeer = null;
        }
    }

    public void onAppVisible() {
        isAppVisible = true;
        hideNotification();
    }

    public void onAppHidden() {
        isAppVisible = false;
    }

    public void onDialogsVisible() {
        isDialogsVisible = true;
        hideNotification();
    }

    public void onDialogsHidden() {
        isDialogsVisible = false;
    }

    private void performNotification(boolean isSilentUpdate) {
        List<PendingNotification> allPending = getNotifications();

        List<PendingNotification> destNotifications = new ArrayList<PendingNotification>();
        for (int i = 0; i < allPending.size(); i++) {
            if (destNotifications.size() >= MAX_NOTIFICATION_COUNT) {
                break;
            }
            PendingNotification pendingNotification = allPending.get(allPending.size() - 1 - i);
            if (visiblePeer != null && visiblePeer.equals(pendingNotification.getPeer())) {
                continue;
            }
            destNotifications.add(pendingNotification);
        }

        if (destNotifications.size() == 0) {
            hideNotification();
            return;
        }

        List<Notification> res = new ArrayList<Notification>();
        for (PendingNotification p : destNotifications) {
            res.add(new Notification(p.getPeer(), p.getSender(), p.getContent()));
        }

        int messagesCount = allPending.size();
        HashSet<PeerEntity> peers = new HashSet<PeerEntity>();
        for (PendingNotification p : allPending) {
            peers.add(p.getPeer());
        }
        int chatsCount = peers.size();

        config().getNotificationProvider().onNotification(context(), res,
                messagesCount, chatsCount, isSilentUpdate, isAppVisible);
    }

    private void hideNotification() {
        config().getNotificationProvider().hideAllNotifications();
    }

    private void saveStorage() {
        this.storage.put(0, pendingStorage.toByteArray());
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof NewMessage) {
            NewMessage newMessage = (NewMessage) message;
            onNewMessage(newMessage.getPeer(), newMessage.getSender(), newMessage.getSortDate(),
                    newMessage.getContentDescription(), newMessage.getHasCurrentUserMention());
        } else if (message instanceof MessagesRead) {
            MessagesRead read = (MessagesRead) message;
            onMessagesRead(read.getPeer(), read.getFromDate());
        } else if (message instanceof OnConversationVisible) {
            onConversationVisible(((OnConversationVisible) message).getPeer());
        } else if (message instanceof OnConversationHidden) {
            onConversationHidden(((OnConversationHidden) message).getPeer());
        } else if (message instanceof OnAppHidden) {
            onAppHidden();
        } else if (message instanceof OnAppVisible) {
            onAppVisible();
        } else if (message instanceof OnDialogsVisible) {
            onDialogsVisible();
        } else if (message instanceof OnDialogsHidden) {
            onDialogsHidden();
        } else if (message instanceof PauseNotifications) {
            onNotificationsPaused();
        } else if (message instanceof ResumeNotifications) {
            onNotificationsResumed();
        } else {
            drop(message);
        }
    }

    public static class NewMessage {
        private PeerEntity peer;
        private int sender;
        private long sortDate;
        private ContentDescription contentDescription;
        private boolean hasCurrentUserMention;

        public NewMessage(PeerEntity peer, int sender, long sortDate, ContentDescription contentDescription,
                          boolean hasCurrentUserMention) {
            this.peer = peer;
            this.sender = sender;
            this.sortDate = sortDate;
            this.contentDescription = contentDescription;
            this.hasCurrentUserMention = hasCurrentUserMention;
        }

        public PeerEntity getPeer() {
            return peer;
        }

        public int getSender() {
            return sender;
        }

        public long getSortDate() {
            return sortDate;
        }

        public ContentDescription getContentDescription() {
            return contentDescription;
        }

        public boolean getHasCurrentUserMention() {
            return hasCurrentUserMention;
        }
    }

    public static class MessagesRead {
        private PeerEntity peer;
        private long fromDate;

        public MessagesRead(PeerEntity peer, long fromDate) {
            this.peer = peer;
            this.fromDate = fromDate;
        }

        public PeerEntity getPeer() {
            return peer;
        }

        public long getFromDate() {
            return fromDate;
        }
    }

    public static class OnConversationVisible {
        private PeerEntity peer;

        public OnConversationVisible(PeerEntity peer) {
            this.peer = peer;
        }

        public PeerEntity getPeer() {
            return peer;
        }
    }

    public static class OnConversationHidden {
        private PeerEntity peer;

        public OnConversationHidden(PeerEntity peer) {
            this.peer = peer;
        }

        public PeerEntity getPeer() {
            return peer;
        }
    }

    public static class OnAppVisible {

    }

    public static class OnAppHidden {

    }

    public static class OnDialogsVisible {

    }

    public static class OnDialogsHidden {

    }

    public static class PauseNotifications {

    }

    public static class ResumeNotifications {

    }
}