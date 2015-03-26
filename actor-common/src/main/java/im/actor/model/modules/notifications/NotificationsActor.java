package im.actor.model.modules.notifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.model.droidkit.engine.SyncKeyValue;
import im.actor.model.entity.ContentDescription;
import im.actor.model.entity.Notification;
import im.actor.model.entity.Peer;
import im.actor.model.modules.Modules;
import im.actor.model.modules.notifications.entity.PendingNotification;
import im.actor.model.modules.notifications.entity.PendingStorage;
import im.actor.model.modules.utils.ModuleActor;

/**
 * Created by ex3ndr on 01.03.15.
 */
public class NotificationsActor extends ModuleActor {

    private static final String PREFERENCES_STORAGE = "notifications_pending";
    private static final int MAX_NOTIFICATION_COUNT = 10;

    private SyncKeyValue storage;
    private PendingStorage pendingStorage;

    private Peer visiblePeer;
    private boolean isAppVisible = false;
    private boolean isDialogsVisible = false;

    public NotificationsActor(Modules messenger) {
        super(messenger);
        this.storage = messenger.getNotifications().getNotificationsStorage();
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

    public void onNewMessage(Peer peer, int sender, long date, ContentDescription description) {

        boolean isEnabled = modules().getSettings().isNotificationsEnabled(peer);
        List<PendingNotification> allPending = getNotifications();

        if (isEnabled) {
            allPending.add(new PendingNotification(peer, sender, date, description));
            saveStorage();
        }

        if (config().getNotificationProvider() != null) {
            if (visiblePeer != null && visiblePeer.equals(peer)) {
                if (modules().getSettings().isConversationTonesEnabled()) {
                    config().getNotificationProvider().onMessageArriveInApp();
                }
                return;
            }
            if (isDialogsVisible) {
                if (modules().getSettings().isConversationTonesEnabled()) {
                    config().getNotificationProvider().onMessageArriveInApp();
                }
                return;
            }

            if (!isEnabled) {
                return;
            }

            List<PendingNotification> destNotifications;
            if (allPending.size() <= MAX_NOTIFICATION_COUNT) {
                destNotifications = new ArrayList<PendingNotification>();
                for (int i = 0; i < allPending.size(); i++) {
                    destNotifications.add(allPending.get(allPending.size() - 1 - i));
                }
            } else {
                destNotifications = new ArrayList<PendingNotification>();
                for (int i = 0; i < MAX_NOTIFICATION_COUNT; i++) {
                    destNotifications.add(allPending.get(allPending.size() - 1 - i));
                }
            }

            List<Notification> res = new ArrayList<Notification>();
            for (PendingNotification p : destNotifications) {
                res.add(new Notification(p.getPeer(), p.getSender(), p.getContent()));
            }

            int messagesCount = allPending.size();
            HashSet<Peer> peers = new HashSet<Peer>();
            for (PendingNotification p : allPending) {
                peers.add(p.getPeer());
            }
            int chatsCount = peers.size();

            config().getNotificationProvider().onNotification(res, messagesCount, chatsCount);
        }
    }

    public void onMessagesRead(Peer peer, long fromDate) {
        boolean isChanged = false;
        for (PendingNotification p : pendingStorage.getNotifications().toArray(new PendingNotification[0])) {
            if (p.getPeer().equals(peer) && p.getDate() <= fromDate) {
                pendingStorage.getNotifications().remove(p);
                isChanged = true;
            }
        }

        if (isChanged) {
            saveStorage();
        }
    }

    public void onConversationVisible(Peer peer) {
        this.visiblePeer = peer;
        if (config().getNotificationProvider() != null) {
            config().getNotificationProvider().onChatOpen(peer);
        }
    }

    public void onConversationHidden(Peer peer) {
        if (visiblePeer != null && visiblePeer.equals(peer)) {
            this.visiblePeer = null;
        }
    }

    public void onAppVisible() {
        isAppVisible = true;
    }

    public void onAppHidden() {
        isAppVisible = false;
    }

    public void onDialogsVisible() {
        isDialogsVisible = true;
        if (config().getNotificationProvider() != null) {
            config().getNotificationProvider().onDialogsOpen();
        }
    }

    public void onDialogsHidden() {
        isDialogsVisible = false;
    }

    private void saveStorage() {
        this.storage.put(0, pendingStorage.toByteArray());
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof NewMessage) {
            NewMessage newMessage = (NewMessage) message;
            onNewMessage(newMessage.getPeer(), newMessage.getSender(),
                    newMessage.getSortDate(), newMessage.getContentDescription());
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
        } else {
            drop(message);
        }
    }

    public static class NewMessage {
        private Peer peer;
        private int sender;
        private long sortDate;
        private ContentDescription contentDescription;

        public NewMessage(Peer peer, int sender, long sortDate, ContentDescription contentDescription) {
            this.peer = peer;
            this.sender = sender;
            this.sortDate = sortDate;
            this.contentDescription = contentDescription;
        }

        public Peer getPeer() {
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
    }

    public static class MessagesRead {
        private Peer peer;
        private long fromDate;

        public MessagesRead(Peer peer, long fromDate) {
            this.peer = peer;
            this.fromDate = fromDate;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getFromDate() {
            return fromDate;
        }
    }

    public static class OnConversationVisible {
        private Peer peer;

        public OnConversationVisible(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
        }
    }

    public static class OnConversationHidden {
        private Peer peer;

        public OnConversationHidden(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
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
}