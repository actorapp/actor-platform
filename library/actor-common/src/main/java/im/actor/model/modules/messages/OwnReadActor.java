/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import im.actor.model.droidkit.engine.SyncKeyValue;
import im.actor.model.entity.ContentDescription;
import im.actor.model.entity.Peer;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.entity.UnreadMessage;
import im.actor.model.modules.messages.entity.UnreadMessagesStorage;
import im.actor.model.modules.utils.ModuleActor;

public class OwnReadActor extends ModuleActor {

    private UnreadMessagesStorage messagesStorage;
    private SyncKeyValue syncKeyValue;

    public OwnReadActor(Modules messenger) {
        super(messenger);
        this.syncKeyValue = messenger.getMessagesModule().getCursorStorage();
    }

    @Override
    public void preStart() {
        super.preStart();

        messagesStorage = new UnreadMessagesStorage();
        byte[] st = syncKeyValue.get(CURSOR_OWN_READ);
        if (st != null) {
            try {
                messagesStorage = UnreadMessagesStorage.fromBytes(st);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onNewInMessage(Peer peer, long rid, long sortingDate, int senderUid, ContentDescription contentDescription, boolean hasCurrentUserMention, boolean isLastInDiff) {
        // Detecting if message already read
        long readState = modules().getMessagesModule().loadReadState(peer);
        boolean isAlreadyRead = sortingDate <= readState;

        // Notify notification actor
        if (contentDescription != null)
            modules().getNotifications().onInMessage(peer, senderUid, sortingDate,
                    contentDescription, hasCurrentUserMention, isAlreadyRead, isLastInDiff);

        if (isAlreadyRead) {
            // Already read
            return;
        }

        // Saving unread message to storage
        HashSet<UnreadMessage> unread = messagesStorage.getUnread(peer);
        unread.add(new UnreadMessage(peer, rid, sortingDate));
        saveStorage();

        // Updating counter
        modules().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.CounterChanged(peer, unread.size()));
    }

    public void onMessageRead(Peer peer, long sortingDate) {
        // Detecting if message already read
        long readState = modules().getMessagesModule().loadReadState(peer);
        if (sortingDate <= readState) {
            // Already read
            return;
        }

        // Marking messages as read
        HashSet<UnreadMessage> unread = messagesStorage.getUnread(peer);

        long maxPlainReadDate = sortingDate;
        boolean removed = false;
        for (UnreadMessage u : unread.toArray(new UnreadMessage[unread.size()])) {
            if (u.getSortDate() <= sortingDate) {
                maxPlainReadDate = Math.max(u.getSortDate(), maxPlainReadDate);
                removed = true;
                unread.remove(u);
            }
        }
        if (removed) {
            saveStorage();
        }

        if (maxPlainReadDate > 0) {
            modules().getMessagesModule().getPlainReadActor()
                    .send(new CursorReaderActor.MarkRead(peer, maxPlainReadDate));
        }

        // Saving last read message
        modules().getMessagesModule().saveReadState(peer, sortingDate);

        // Updating counter
        modules().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.CounterChanged(peer, unread.size()));

        modules().getNotifications().onOwnRead(peer, sortingDate);
    }

    public void onMessageReadByMe(Peer peer, long sortingDate) {

        long msgSortingDate = 0;

        // Finding suitable message
        Set<UnreadMessage> unread = messagesStorage.getUnread(peer);
        for (UnreadMessage u : unread.toArray(new UnreadMessage[unread.size()])) {
            if (u.getSortDate() <= sortingDate && u.getSortDate() > msgSortingDate) {
                msgSortingDate = u.getSortDate();
            }
        }

        if (msgSortingDate > 0) {
            onMessageRead(peer, msgSortingDate);
        }
    }

    public void onMessageDelete(Peer peer, List<Long> rids) {
        Set<UnreadMessage> unread = messagesStorage.getUnread(peer);
        boolean isRemoved = false;
        for (UnreadMessage u : unread.toArray(new UnreadMessage[unread.size()])) {
            if (rids.contains(u.getRid())) {
                unread.remove(u);
                isRemoved = true;
            }
        }
        if (!isRemoved) {
            return;
        }

        saveStorage();

        // Updating counter
        modules().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.CounterChanged(peer, unread.size()));

        // TODO: Notify delete
    }

    private void saveStorage() {
        syncKeyValue.put(CURSOR_OWN_READ, messagesStorage.toByteArray());
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof NewMessage) {
            NewMessage newMessage = (NewMessage) message;
            onNewInMessage(newMessage.getPeer(), newMessage.getRid(), newMessage.getSortingDate(), newMessage.getSenderUId(), newMessage.getContentDescription(), newMessage.getHasCurrentUserMention(), newMessage.isLastInDiff());
        } else if (message instanceof MessageRead) {
            MessageRead messageRead = (MessageRead) message;
            onMessageRead(messageRead.getPeer(), messageRead.getSortingDate());
        } else if (message instanceof MessageReadByMe) {
            MessageReadByMe readByMe = (MessageReadByMe) message;
            onMessageReadByMe(readByMe.getPeer(), readByMe.getSortDate());
        } else if (message instanceof MessageDeleted) {
            MessageDeleted deleted = (MessageDeleted) message;
            onMessageDelete(deleted.getPeer(), deleted.getRids());
        } else {
            drop(message);
        }
    }

    public static class MessageReadByMe {
        Peer peer;
        long sortDate;

        public MessageReadByMe(Peer peer, long sortDate) {
            this.peer = peer;
            this.sortDate = sortDate;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getSortDate() {
            return sortDate;
        }
    }

    public static class MessageRead {
        Peer peer;
        long sortingDate;

        public MessageRead(Peer peer, long sortingDate) {
            this.peer = peer;
            this.sortingDate = sortingDate;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getSortingDate() {
            return sortingDate;
        }
    }

    public static class NewMessage {
        boolean hasCurrentUserMention;
        Peer peer;
        long rid;
        long sortingDate;
        int senderUId;
        ContentDescription contentDescription;
        boolean isLastInDiff;

        public NewMessage(Peer peer, long rid, long sortingDate) {
            this.peer = peer;
            this.rid = rid;
            this.sortingDate = sortingDate;
        }

        public NewMessage(Peer peer, long rid, long sortingDate, int senderUId, ContentDescription contentDescription,
                          boolean hasCurrentUserMention, boolean isLastInDiff) {
            this.peer = peer;
            this.rid = rid;
            this.sortingDate = sortingDate;
            this.senderUId = senderUId;
            this.contentDescription = contentDescription;
            this.hasCurrentUserMention = hasCurrentUserMention;
            this.isLastInDiff = isLastInDiff;
        }

        public int getSenderUId() { return senderUId; }

        public ContentDescription getContentDescription() { return contentDescription; }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }

        public long getSortingDate() {
            return sortingDate;
        }

        public boolean getHasCurrentUserMention() {
            return hasCurrentUserMention;
        }

        public boolean isLastInDiff() {
            return isLastInDiff;
        }
    }

    public static class MessageDeleted {
        Peer peer;
        List<Long> rids;

        public MessageDeleted(Peer peer, List<Long> rids) {
            this.peer = peer;
            this.rids = rids;
        }

        public Peer getPeer() {
            return peer;
        }

        public List<Long> getRids() {
            return rids;
        }
    }
}
