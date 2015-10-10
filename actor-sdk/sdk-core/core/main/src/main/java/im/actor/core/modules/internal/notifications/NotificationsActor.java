/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.notifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import im.actor.core.PlatformType;
import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.Notification;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.events.AppVisibleChanged;
import im.actor.core.modules.events.PeerChatClosed;
import im.actor.core.modules.events.PeerChatOpened;
import im.actor.core.modules.internal.notifications.entity.PendingNotification;
import im.actor.core.modules.internal.notifications.entity.PendingStorage;
import im.actor.core.modules.internal.notifications.entity.ReadState;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.runtime.Storage;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.storage.SyncKeyValue;

import static im.actor.core.util.JavaUtil.last;

/**
 * Actor that controls all notifications in application
 * <p/>
 * NotificationsActor keeps all unread messages for showing last unread messages in notifications
 * Actor also control sound effects playing logic
 */
public class NotificationsActor extends ModuleActor {

    /**
     * Maximum amount of messages in notification
     */
    private static final int MAX_NOTIFICATION_COUNT = 10;

    /**
     * KeyValue storage name for actor state
     */
    private static final String STORAGE_NOTIFICATIONS = "notifications";


    /**
     * Storage for Actor internal state
     */
    private SyncKeyValue storage;
    /**
     * BSer object for pending notifications storage
     */
    private PendingStorage pendingStorage;
    /**
     * Cached read states
     */
    private HashMap<Peer, Long> readStates = new HashMap<Peer, Long>();


    /**
     * Current visible peer
     */
    private Peer visiblePeer;
    /**
     * Is Application visible state
     */
    private boolean isAppVisible = false;


    /**
     * Is Notifications paused
     */
    private boolean isNotificationsPaused = false;
    /**
     * Stored notifications during pause
     */
    private HashMap<Peer, Boolean> notificationsDuringPause = new HashMap<Peer, Boolean>();


    /**
     * Is current platform is mobile
     */
    private boolean isMobilePlatform = false;

    /**
     * Constructor of Actor
     *
     * @param context Module context
     */
    public NotificationsActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {

        isMobilePlatform = config().getPlatformType() == PlatformType.ANDROID ||
                config().getPlatformType() == PlatformType.IOS;

        // Building storage
        storage = new SyncKeyValue(Storage.createKeyValue(STORAGE_NOTIFICATIONS));

        // Loading pending messages
        pendingStorage = new PendingStorage();
        byte[] storage = this.storage.get(0);
        if (storage != null) {
            try {
                pendingStorage = PendingStorage.fromBytes(storage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        subscribe(AppVisibleChanged.EVENT);
        subscribe(PeerChatOpened.EVENT);
        subscribe(PeerChatClosed.EVENT);
    }

    /**
     * Handling event about incoming notification
     *
     * @param peer                  peer of message
     * @param sender                sender uid of message
     * @param date                  date of message
     * @param description           content description of message
     * @param hasCurrentUserMention does message have user mention
     */
    public void onNewMessage(Peer peer, int sender, long date, ContentDescription description,
                             boolean hasCurrentUserMention) {

        // Check if message already read to avoid incorrect notifications
        // for already read messages
        if (date <= getLastReadDate(peer)) {
            return;
        }

        // If wee need to play "out-app" notification for message
        boolean isEnabled = isNotificationsEnabled(peer, hasCurrentUserMention);

        // Save to pending storage
        if (isEnabled) {
            List<PendingNotification> allPending = getNotifications();
            allPending.add(new PendingNotification(peer, sender, date, description));
            saveStorage();
        }

        // Adding to paused cache if notifications paused
        // This peers will be released during call onNotificationsResumed
        if (isNotificationsPaused) {
            if (!notificationsDuringPause.containsKey(peer)) {
                notificationsDuringPause.put(peer, hasCurrentUserMention);
            } else {
                if (hasCurrentUserMention && !notificationsDuringPause.get(peer)) {
                    notificationsDuringPause.put(peer, true);
                }

            }
            return;
        }

        // Performing notification
        //
        // There are two separate modes of notifications:
        // First one is when app is visible, and second one when not.
        //
        // For in app notifications app need to be more careful as people already pay
        // full attention to app or may be particular chat.
        // For "in-app" notifications playing only sound effects for new messages.
        //
        // For "out-app" just playing default notifications.
        //
        // WARRING: This implementation is copied to onNotificationsResumed with small changes
        // to fit logic

        if (isAppVisible) {

            // If application is visible application play only sound effects and
            // doesn't show any "out-app" notifications

            if (visiblePeer != null && visiblePeer.equals(peer)) {

                // If message arrive to visible chat
                if (isMobilePlatform) {

                    // Play sound effect on mobile
                    playEffectIfEnabled();
                } else {

                    // Do nothing on desktop as it is very distracting
                    // Most of the people use mobile messengers occasionally and
                    // with disabled sounds and it doesn't make problems as much as on
                    // non-mobile platforms
                }
            } else {

                if (isMobilePlatform) {

                    // Don't play any sounds not in chat screen on mobile phone
                    // We done this because we have unread badge in chat screen on mobile
                } else {

                    // For desktop environments play sounds for all chats that
                    // doesn't explicitly disabled
                    if (isEnabled) {
                        playEffectIfEnabled();
                    }
                }
            }

        } else {

            // If application is not visible play regular notification for enabled chats
            if (isEnabled) {
                showNotification();
            }
        }
    }

    /**
     * Processing event about messages read
     *
     * @param peer     peer
     * @param fromDate read from date
     */
    public void onMessagesRead(Peer peer, long fromDate) {

        // Filtering obsolete read events
        if (fromDate < getLastReadDate(peer)) {
            return;
        }

        // Removing read messages from pending storage
        boolean isChanged = false;
        List<PendingNotification> notifications = pendingStorage.getNotifications();
        for (PendingNotification p : notifications.toArray(new PendingNotification[notifications.size()])) {
            if (p.getPeer().equals(peer) && p.getDate() <= fromDate) {
                pendingStorage.getNotifications().remove(p);
                isChanged = true;
            }
        }

        // If there are some messages
        // Save pending and update notification
        if (isChanged) {
            saveStorage();
            updateNotification();
        }

        // Setting last read date
        setLastReadDate(peer, fromDate);
    }

    /**
     * Pausing notifications
     */
    public void onNotificationsPaused() {
        isNotificationsPaused = true;
    }

    /**
     * Resuming notifications.
     * Checking all pending notification peers and play notifications if it is required.
     * WARRING: Implementation contains modified copy of code of onNewMessage method
     */
    public void onNotificationsResumed() {
        isNotificationsPaused = false;

        // If there are notifications during pause
        if (notificationsDuringPause.size() > 0) {

            if (isAppVisible) {
                if (visiblePeer != null && notificationsDuringPause.containsKey(visiblePeer)) {

                    // If there was message from visible chat
                    if (isMobilePlatform) {

                        // Playing sound effects for mobile platforms
                        playEffectIfEnabled();
                    } else {

                        // Don't play sounds in chat on non-mobile platforms
                    }
                } else {

                    // If there are no messages from visible peer
                    if (isMobilePlatform) {

                        // Don't play sounds not from current chat on mobile platforms
                    } else {

                        // Find any suitable peer and if found play sound effect
                        for (Peer p : notificationsDuringPause.keySet()) {

                            if (isNotificationsEnabled(p, notificationsDuringPause.get(p))) {
                                playEffectIfEnabled();
                                break; // Can't do return because we need to make more work later
                            }
                        }
                    }
                }
            } else {

                // Just show out-app notification
                showNotification();
            }

            // Clearing of notifications
            notificationsDuringPause.clear();
        }
    }

    /**
     * Processing Conversation visible event
     *
     * @param peer peer
     */
    public void onConversationVisible(Peer peer) {
        this.visiblePeer = peer;
    }

    /**
     * Processing Conversation hidden event
     *
     * @param peer peer
     */
    public void onConversationHidden(Peer peer) {
        if (visiblePeer != null && visiblePeer.equals(peer)) {
            this.visiblePeer = null;
        }
    }

    /**
     * Processing Application visible event
     */
    public void onAppVisible() {
        isAppVisible = true;

        // Hiding all notifications right after opening application
        hideNotification();
    }

    /**
     * Processing Application hidden event
     */
    public void onAppHidden() {
        isAppVisible = false;
    }

    /**
     * Performing notifications
     */

    /**
     * Playing sound effects
     */
    private void playEffectIfEnabled() {
        if (isEffectsEnabled()) {
            config().getNotificationProvider().onMessageArriveInApp(context().getMessenger());
        }
    }

    /**
     * Updating notifications
     */
    private void updateNotification() {
        performNotificationImp(true);
    }

    /**
     * Showing new notifications
     */
    private void showNotification() {
        performNotificationImp(false);
    }

    /**
     * Hiding notifications
     */
    private void hideNotification() {
        config().getNotificationProvider().hideAllNotifications();
    }

    /**
     * Method for showing/updating notifications
     *
     * @param performUpdate is need to perform update instead of showing
     */
    private void performNotificationImp(boolean performUpdate) {
        // Getting pending notifications list
        List<PendingNotification> allPending = getNotifications();
        int messagesCount = allPending.size();
        if (messagesCount == 0) {
            hideNotification();
            return;
        }

        // Destination notifications list
        List<PendingNotification> destNotifications = last(allPending, MAX_NOTIFICATION_COUNT);

        // Converting to PendingNotifications
        List<Notification> res = new ArrayList<Notification>();
        for (PendingNotification p : destNotifications) {
            res.add(new Notification(p.getPeer(), p.getSender(), p.getContent()));
        }

        // Getting count of unique peers
        HashSet<Peer> peers = new HashSet<Peer>();
        for (PendingNotification p : allPending) {
            peers.add(p.getPeer());
        }
        int chatsCount = peers.size();

        // Performing notifications
        if (performUpdate) {
            config().getNotificationProvider().onUpdateNotification(context().getMessenger(), res,
                    messagesCount, chatsCount);
        } else {
            config().getNotificationProvider().onNotification(context().getMessenger(), res,
                    messagesCount, chatsCount);
        }
    }

    /**
     * Storage and settings methods
     */

    /**
     * Convenience method for checking if sound effects are enabled
     *
     * @return is sound effects are enabled
     */
    private boolean isEffectsEnabled() {
        return context().getSettingsModule().isConversationTonesEnabled();
    }

    /**
     * Testing if notifications enabled for message
     *
     * @param peer       peer of message
     * @param hasMention does peer have mention
     * @return is notification enabled for peer
     */
    private boolean isNotificationsEnabled(Peer peer, boolean hasMention) {

        // If notifications doesn't enabled at all
        if (!context().getSettingsModule().isNotificationsEnabled()) {
            return false;
        }

        // Notifications for groups
        if (peer.getPeerType() == PeerType.GROUP) {

            // Disable notifications for hidden groups
            if (getGroup(peer.getPeerId()).isHidden()) {
                return false;
            }

            if (context().getSettingsModule().isGroupNotificationsEnabled()) {

                // If there are mention in group always allow notification
                if (hasMention) {
                    return true;
                }

                if (context().getSettingsModule().isNotificationsEnabled(peer)) {

                    // If forced only mentions
                    if (context().getSettingsModule().isGroupNotificationsOnlyMentionsEnabled()) {
                        return false; // hasMention always false at this line
                    } else {
                        return true;
                    }
                } else {

                    // Notifications are not enabled in group
                    return false;
                }
            } else {

                // All group notifications are disabled
                return false;
            }
        } else if (peer.getPeerType() == PeerType.PRIVATE) {

            // For private conversations only check if peer notifications enabled
            return context().getSettingsModule().isNotificationsEnabled(peer);
        } else {

            // Never happens
            throw new RuntimeException("Unknown peer type");
        }
    }

    /**
     * Convenience method for getting all notifications
     *
     * @return all pending notifications
     */
    private List<PendingNotification> getNotifications() {
        return pendingStorage.getNotifications();
    }

    /**
     * Saving pending messages storage
     */
    private void saveStorage() {
        this.storage.put(0, pendingStorage.toByteArray());
    }

    /**
     * Getting last read sort key for peer
     *
     * @param peer peer for key
     * @return sort key, 0 if not available
     */
    private long getLastReadDate(Peer peer) {
        if (readStates.containsKey(peer)) {
            return readStates.get(peer);
        }

        byte[] data = storage.get(peer.getUnuqueId());
        if (data != null) {
            try {
                return ReadState.fromBytes(data).getSortDate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Setting last read date for peer
     *
     * @param peer peer
     * @param date date
     */
    private void setLastReadDate(Peer peer, long date) {
        storage.put(peer.getUnuqueId(), new ReadState(date).toByteArray());
        readStates.put(peer, date);
    }

    /**
     * Actor stuff
     */

    /**
     * Receiving messages
     *
     * @param message message
     */
    @Override
    public void onReceive(Object message) {
        if (message instanceof NewMessage) {
            NewMessage newMessage = (NewMessage) message;
            onNewMessage(newMessage.getPeer(), newMessage.getSender(), newMessage.getSortDate(),
                    newMessage.getContentDescription(), newMessage.getHasCurrentUserMention());
        } else if (message instanceof MessagesRead) {
            MessagesRead read = (MessagesRead) message;
            onMessagesRead(read.getPeer(), read.getFromDate());
        } else if (message instanceof PauseNotifications) {
            onNotificationsPaused();
        } else if (message instanceof ResumeNotifications) {
            onNotificationsResumed();
        } else {
            drop(message);
        }
    }

    /**
     * Receiving bus events
     *
     * @param event event
     */
    @Override
    public void onBusEvent(Event event) {
        if (event instanceof AppVisibleChanged) {
            AppVisibleChanged visibleChanged = (AppVisibleChanged) event;
            if (visibleChanged.isVisible()) {
                onAppVisible();
            } else {
                onAppHidden();
            }
        } else if (event instanceof PeerChatOpened) {
            onConversationVisible(((PeerChatOpened) event).getPeer());
        } else if (event instanceof PeerChatClosed) {
            onConversationHidden(((PeerChatClosed) event).getPeer());
        }
    }

    public static class NewMessage {
        private Peer peer;
        private int sender;
        private long sortDate;
        private ContentDescription contentDescription;
        private boolean hasCurrentUserMention;

        public NewMessage(Peer peer, int sender, long sortDate, ContentDescription contentDescription,
                          boolean hasCurrentUserMention) {
            this.peer = peer;
            this.sender = sender;
            this.sortDate = sortDate;
            this.contentDescription = contentDescription;
            this.hasCurrentUserMention = hasCurrentUserMention;
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

        public boolean getHasCurrentUserMention() {
            return hasCurrentUserMention;
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

    public static class PauseNotifications {

    }

    public static class ResumeNotifications {

    }
}
