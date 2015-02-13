package im.actor.messenger.core.actors.chat;

import com.droidkit.actors.typed.TypedActor;
import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.persistence.BserMap;
import com.droidkit.engine.persistence.PersistenceSet;
import com.droidkit.engine.persistence.storage.SqliteStorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.core.actors.messages.PlainReadActor;
import im.actor.messenger.core.actors.messages.ReadEncryptedActor;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.storage.DbProvider;
import im.actor.messenger.storage.scheme.messages.ReadState;

import static im.actor.messenger.storage.KeyValueEngines.readStates;

/**
 * Created by ex3ndr on 24.10.14.
 */
public class OwnReadStateActor extends TypedActor<OwnReadStateInt> implements OwnReadStateInt {

    private static TypedActorHolder<OwnReadStateInt> HOLDER = new TypedActorHolder<OwnReadStateInt>(OwnReadStateInt.class,
            OwnReadStateActor.class, "read_state");

    public static OwnReadStateInt readState() {
        return HOLDER.get();
    }

    private HashMap<Long, PersistenceSet<UnreadMessage>> unreadsEncryptedCache = new HashMap<Long, PersistenceSet<UnreadMessage>>();

    private HashMap<Long, PersistenceSet<UnreadMessage>> unreadsPlainCache = new HashMap<Long, PersistenceSet<UnreadMessage>>();

    public OwnReadStateActor() {
        super(OwnReadStateInt.class);
    }

    /**
     * On any outcoming message we mark every message in conversation as read and
     * resetting unread counter in dialog list.
     *
     * @param chatType
     * @param chatId
     * @param rid
     * @param sortingKey
     * @param date
     * @param isEncrypted
     */
    @Override
    public void newOutMessage(int chatType, int chatId, long rid, long sortingKey, long date,
                              boolean isEncrypted) {
        long uid = DialogUids.getDialogUid(chatType, chatId);

        // Marking all unread encrypted messages as read
        {
            Set<UnreadMessage> unread = getEncryptedUnread(uid);

            for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
                system().actorOf(ReadEncryptedActor.messageReader())
                        .send(new ReadEncryptedActor.Read(chatType, chatId, u.rid));
                unread.remove(u);
            }
        }

        // Marking all previous plain messages as read

        long maxPlainReadDate = 0;
        {
            Set<UnreadMessage> unread = getPlainUnread(uid);
            if (unread.size() > 0) {
                for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
                    maxPlainReadDate = Math.max(u.date, maxPlainReadDate);
                }
                unread.clear();
            }
        }

        if (maxPlainReadDate > 0) {
            PlainReadActor.plainRead().markRead(chatType, chatId, maxPlainReadDate);
        }

        // Saving last read message
        readStates().put(new ReadState(chatType, chatId, rid, sortingKey));

        // Resetting counter
        DialogsActor.dialogs().onCounterChanged(chatType, chatId, 0);
    }

    /**
     * On any income message we search if message already read by checking sortingKey
     * of saved read state and new message.
     * If encrypted message already read then sending request to server and exiting.
     * If plain message then already read exit method.
     * <p/>
     * Elsewhere add to encrypted/plain unread storage and update dialogs counter
     *
     * @param chatType
     * @param chatId
     * @param rid
     * @param sortingKey
     * @param date
     * @param isEncrypted
     */
    @Override
    public void newMessage(int chatType, int chatId, long rid, long sortingKey, long date,
                           boolean isEncrypted) {
        long uid = DialogUids.getDialogUid(chatType, chatId);

        // Detecting if message already read
        ReadState readState = readStates().get(uid);
        if (readState != null && sortingKey <= readState.getLastReadSortingKey()) {
            // Already read
            if (isEncrypted) {
                system().actorOf(ReadEncryptedActor.messageReader())
                        .send(new ReadEncryptedActor.Read(chatType, chatId, rid));
            } else {
                // Nothing to do for plain messages
            }
            return;
        }

        // Saving unread message to storage
        if (isEncrypted) {
            getEncryptedUnread(uid).add(new UnreadMessage(rid, sortingKey, date));
        } else {
            getPlainUnread(uid).add(new UnreadMessage(rid, sortingKey, date));
        }

        // Updating counter
        DialogsActor.dialogs().onCounterChanged(chatType, chatId,
                getEncryptedUnread(uid).size() +
                        getPlainUnread(uid).size());
    }

    /**
     * First of all check if we already read message by comparing sortingKeys.
     * If already read - exiting.
     * <p/>
     * Elsewhere marking all previous and current messages as read.
     * We need to mark encrypted and plain messages.
     *
     * @param chatType
     * @param chatId
     * @param rid
     * @param sortingKey
     * @param date
     * @param isEncrypted
     */
    @Override
    public void messageRead(int chatType, int chatId, long rid, long sortingKey, long date,
                            boolean isEncrypted) {
        long uid = DialogUids.getDialogUid(chatType, chatId);

        // Detecting if message already read
        ReadState readState = readStates().get(uid);
        if (readState != null && sortingKey <= readState.getLastReadSortingKey()) {
            // Already read
            return;
        }

        // Marking encrypted messages as read
        {
            Set<UnreadMessage> unread = getEncryptedUnread(uid);

            // Finding and marking all previous messages
            for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
                if (u.sortKey <= sortingKey) {
                    system().actorOf(ReadEncryptedActor.messageReader())
                            .send(new ReadEncryptedActor.Read(chatType, chatId, u.rid));
                    unread.remove(u);
                }
            }
        }

        // Marking plain messages as read
        long maxPlainReadDate = 0;
        if (!isEncrypted) {
            maxPlainReadDate = date;
        }
        {
            Set<UnreadMessage> unread = getPlainUnread(uid);

            for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
                if (u.sortKey <= sortingKey) {
                    maxPlainReadDate = Math.max(u.date, maxPlainReadDate);
                    unread.remove(u);
                }
            }
        }

        if (isEncrypted) {
            // Marking current encrypted message
            system().actorOf(ReadEncryptedActor.messageReader())
                    .send(new ReadEncryptedActor.Read(chatType, chatId, rid));
        }

        if (maxPlainReadDate > 0) {
            PlainReadActor.plainRead().markRead(chatType, chatId, maxPlainReadDate);
        }

        // Saving last read message
        readStates().put(new ReadState(chatType, chatId, rid, sortingKey));

        // Updating counter
        DialogsActor.dialogs().onCounterChanged(chatType, chatId,
                getEncryptedUnread(uid).size() +
                        getPlainUnread(uid).size());
    }

    /**
     * Find max unread message and pass it to messageRead
     *
     * @param chatType
     * @param chatId
     * @param date
     */
    @Override
    public void messagePlainReadByMe(int chatType, int chatId, long date) {
        long uid = DialogUids.getDialogUid(chatType, chatId);

        long msgRid = 0;
        long msgDate = 0;
        long msgSortingKey = 0;

        // Finding suitable message
        Set<UnreadMessage> unread = getPlainUnread(uid);
        for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
            if (u.date <= date && u.date > msgDate) {
                msgSortingKey = u.sortKey;
                msgRid = u.rid;
                msgDate = u.date;
            }
        }

        if (msgSortingKey > 0) {
            messageRead(chatType, chatId, msgRid, msgSortingKey, msgDate, false);
        }
    }

    /**
     * Find encrypted unread message and pass it to messageRead
     *
     * @param chatType
     * @param chatId
     * @param rid
     */
    @Override
    public void messageEncryptedReadByMe(int chatType, int chatId, long rid) {
        long uid = DialogUids.getDialogUid(chatType, chatId);

        UnreadMessage unreadMessage = null;

        Set<UnreadMessage> unread = getEncryptedUnread(uid);

        for (UnreadMessage u : unread.toArray(new UnreadMessage[0])) {
            if (u.rid == rid) {
                unreadMessage = u;
                break;
            }
        }

        if (unreadMessage != null) {
            messageRead(chatType, chatId, unreadMessage.rid, unreadMessage.sortKey,
                    unreadMessage.date, true);
        }
    }

    private Set<UnreadMessage> getEncryptedUnread(long uid) {
        if (unreadsEncryptedCache.containsKey(uid)) {
            return unreadsEncryptedCache.get(uid);
        }
        BserMap<UnreadMessage> map = new BserMap<UnreadMessage>(new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()),
                "unread_" + uid), UnreadMessage.class);
        PersistenceSet<UnreadMessage> res = new PersistenceSet<UnreadMessage>(map);
        unreadsEncryptedCache.put(uid, res);
        return res;
    }

    private Set<UnreadMessage> getPlainUnread(long uid) {
        if (unreadsPlainCache.containsKey(uid)) {
            return unreadsPlainCache.get(uid);
        }
        BserMap<UnreadMessage> map = new BserMap<UnreadMessage>(new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()),
                "plain_unread_" + uid), UnreadMessage.class);
        PersistenceSet<UnreadMessage> res = new PersistenceSet<UnreadMessage>(map);
        unreadsPlainCache.put(uid, res);
        return res;
    }

    public static class UnreadMessage extends BserObject {
        private long rid;
        private long sortKey;
        private long date;

        public UnreadMessage(long rid, long sortKey, long date) {
            this.rid = rid;
            this.sortKey = sortKey;
            this.date = date;
        }

        public UnreadMessage() {
        }

        @Override

        public void parse(BserValues values) throws IOException {
            rid = values.getLong(1);
            sortKey = values.getLong(2);
            date = values.getLong(3);
        }

        @Override
        public void serialize(BserWriter writer) throws IOException {
            writer.writeLong(1, rid);
            writer.writeLong(2, sortKey);
            writer.writeLong(3, date);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UnreadMessage that = (UnreadMessage) o;

            if (rid != that.rid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (rid ^ (rid >>> 32));
        }
    }
}
