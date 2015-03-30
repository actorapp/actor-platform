package im.actor.model.modules.messages;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import im.actor.model.droidkit.engine.SyncKeyValue;
import im.actor.model.entity.Peer;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.entity.UnreadMessage;
import im.actor.model.modules.messages.entity.UnreadMessagesStorage;
import im.actor.model.modules.utils.ModuleActor;

/**
 * Created by ex3ndr on 17.02.15.
 */
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

    public void onNewOutMessage(Peer peer, long rid, long sortingDate, boolean isEncrypted) {

        Set<UnreadMessage> unread = messagesStorage.getUnread(peer);

        // Marking all unread encrypted messages as read
        long maxPlainReadDate = 0;
        for (UnreadMessage u : unread) {
            if (u.isEncrypted()) {
                // TODO: Notify about encrypted message read
            } else {
                maxPlainReadDate = Math.max(u.getSortDate(), maxPlainReadDate);
            }
        }
        unread.clear();

        if (maxPlainReadDate > 0) {
            modules().getMessagesModule().getPlainReadActor()
                    .send(new PlainReaderActor.MarkRead(peer, maxPlainReadDate));
        }

        // Saving last read message
        modules().getMessagesModule().saveReadState(peer, sortingDate);

        // Resetting counter
        modules().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.CounterChanged(peer, 0));
    }

    public void onNewInMessage(Peer peer, long rid, long sortingDate, boolean isEncrypted) {
        // Detecting if message already read
        long readState = modules().getMessagesModule().loadReadState(peer);
        if (sortingDate <= readState) {
            // Already read
            if (isEncrypted) {
                // TODO: Notify about encrypted message read
            } else {
                // Nothing to do for plain messages: already read
            }
        }

        // Saving unread message to storage
        HashSet<UnreadMessage> unread = messagesStorage.getUnread(peer);
        unread.add(new UnreadMessage(peer, rid, sortingDate, isEncrypted));
        saveStorage();

        // Updating counter
        modules().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.CounterChanged(peer, unread.size()));
    }

    public void onMessageRead(Peer peer, long rid, long sortingDate, boolean isEncrypted) {
        // Detecting if message already read
        long readState = modules().getMessagesModule().loadReadState(peer);
        if (sortingDate <= readState) {
            // Already read
            return;
        }

        // Marking messages as read
        HashSet<UnreadMessage> unread = messagesStorage.getUnread(peer);

        long maxPlainReadDate = 0;
        if (!isEncrypted) {
            maxPlainReadDate = sortingDate;
        }
        boolean removed = false;
        for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
            if (u.getSortDate() <= sortingDate) {
                if (u.isEncrypted()) {
                    // TODO: Notify about encrypted message read
                } else {
                    // Updating plain read date
                    maxPlainReadDate = Math.max(u.getSortDate(), maxPlainReadDate);
                }
                removed = true;
                unread.remove(u);
            }
        }
        if (removed) {
            saveStorage();
        }

        if (isEncrypted) {
            // Marking current encrypted message
//            system().actorOf(ReadEncryptedActor.messageReader())
//                    .send(new ReadEncryptedActor.Read(chatType, chatId, rid));
            // TODO: Notify about this encrypted message read
        }

        if (maxPlainReadDate > 0) {
            modules().getMessagesModule().getPlainReadActor()
                    .send(new PlainReaderActor.MarkRead(peer, maxPlainReadDate));
        }

        // Saving last read message
        modules().getMessagesModule().saveReadState(peer, sortingDate);

        // Updating counter
        modules().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.CounterChanged(peer, unread.size()));

        modules().getNotifications().onOwnRead(peer, sortingDate);
    }

    public void onMessageReadByMe(Peer peer, long sortingDate) {
        long msgRid = 0;
        long msgSortingDate = 0;

        // Finding suitable message
        Set<UnreadMessage> unread = messagesStorage.getUnread(peer);
        for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
            if (u.isEncrypted()) {
                continue;
            }
            if (u.getSortDate() <= sortingDate && u.getSortDate() > msgSortingDate) {
                msgSortingDate = u.getSortDate();
                msgRid = u.getRid();
            }
        }

        if (msgSortingDate > 0) {
            onMessageRead(peer, msgRid, msgSortingDate, false);
        }
    }

    public void onMessageReadByMeEncrypted(Peer peer, long rid) {
        UnreadMessage unreadMessage = null;

        Set<UnreadMessage> unread = messagesStorage.getUnread(peer);
        for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
            if (!u.isEncrypted()) {
                continue;
            }
            if (u.getRid() == rid) {
                unreadMessage = u;
                break;
            }
        }

        if (unreadMessage != null) {
            onMessageRead(peer, unreadMessage.getRid(), unreadMessage.getSortDate(), true);
        }
    }

    public void onMessageDelete(Peer peer, List<Long> rids) {
        Set<UnreadMessage> unread = messagesStorage.getUnread(peer);
        boolean isRemoved = false;
        for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
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
        if (message instanceof NewOutMessage) {
            NewOutMessage outMessage = (NewOutMessage) message;
            onNewOutMessage(outMessage.getPeer(), outMessage.getRid(), outMessage.getSortingDate(),
                    outMessage.isEncrypted());
        } else if (message instanceof NewMessage) {
            NewMessage newMessage = (NewMessage) message;
            onNewInMessage(newMessage.getPeer(), newMessage.getRid(), newMessage.getSortingDate(),
                    newMessage.isEncrypted());
        } else if (message instanceof MessageRead) {
            MessageRead messageRead = (MessageRead) message;
            onMessageRead(messageRead.getPeer(), messageRead.getRid(), messageRead.getSortingDate(),
                    messageRead.isEncrypted());
        } else if (message instanceof MessageReadByMe) {
            MessageReadByMe readByMe = (MessageReadByMe) message;
            onMessageReadByMe(readByMe.getPeer(), readByMe.getSortDate());
        } else if (message instanceof MessageReadByMeEncrypted) {
            MessageReadByMeEncrypted readByMeEncrypted = (MessageReadByMeEncrypted) message;
            onMessageReadByMeEncrypted(readByMeEncrypted.getPeer(), readByMeEncrypted.getRid());
        } else if (message instanceof MessageDeleted) {
            MessageDeleted deleted = (MessageDeleted) message;
            onMessageDelete(deleted.getPeer(), deleted.getRids());
        } else {
            drop(message);
        }
    }

    public static class MessageReadByMeEncrypted {
        Peer peer;
        long rid;

        public MessageReadByMeEncrypted(Peer peer, long rid) {
            this.peer = peer;
            this.rid = rid;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
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
        long rid;
        long sortingDate;
        boolean isEncrypted;

        public MessageRead(Peer peer, long rid, long sortingDate, boolean isEncrypted) {
            this.peer = peer;
            this.rid = rid;
            this.sortingDate = sortingDate;
            this.isEncrypted = isEncrypted;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }

        public long getSortingDate() {
            return sortingDate;
        }

        public boolean isEncrypted() {
            return isEncrypted;
        }
    }

    public static class NewOutMessage {
        Peer peer;
        long rid;
        long sortingDate;
        boolean isEncrypted;

        public NewOutMessage(Peer peer, long rid, long sortingDate, boolean isEncrypted) {
            this.peer = peer;
            this.rid = rid;
            this.sortingDate = sortingDate;
            this.isEncrypted = isEncrypted;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }

        public long getSortingDate() {
            return sortingDate;
        }

        public boolean isEncrypted() {
            return isEncrypted;
        }
    }

    public static class NewMessage {
        Peer peer;
        long rid;
        long sortingDate;
        boolean isEncrypted;

        public NewMessage(Peer peer, long rid, long sortingDate, boolean isEncrypted) {
            this.peer = peer;
            this.rid = rid;
            this.sortingDate = sortingDate;
            this.isEncrypted = isEncrypted;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }

        public long getSortingDate() {
            return sortingDate;
        }

        public boolean isEncrypted() {
            return isEncrypted;
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
