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
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.storage.DbProvider;

/**
 * Created by ex3ndr on 19.11.14.
 */
public class ReadStateActor extends TypedActor<ReadStateInt> implements ReadStateInt {

    private static final TypedActorHolder<ReadStateInt> HOLDER = new TypedActorHolder<ReadStateInt>(
            ReadStateInt.class, ReadStateActor.class, "remote_reader");

    public static ReadStateInt readState() {
        return HOLDER.get();
    }

    public ReadStateActor() {
        super(ReadStateInt.class);
    }

    private HashMap<Long, PersistenceSet<PendingMessage>> pendingCache =
            new HashMap<Long, PersistenceSet<PendingMessage>>();

    @Override
    public void onNewOutMessage(int chatType, int chatId, long date, long rid, boolean isEncrypted) {
        if (isEncrypted) {
            // We don't need to keep index for encrypted messages
            return;
        }
        long uid = DialogUids.getDialogUid(chatType, chatId);
        Set<PendingMessage> pendingMessages = getPlainUnread(uid);
        pendingMessages.add(new PendingMessage(rid, date, STATE_NONE));
    }

    @Override
    public void markMessagesRead(int chatType, int chatId, long date) {
        long uid = DialogUids.getDialogUid(chatType, chatId);
        Set<PendingMessage> pendingMessages = getPlainUnread(uid);
        for (PendingMessage p : pendingMessages.toArray(new PendingMessage[0])) {
            if (p.date <= date) {
                ConversationActor.conv(chatType, chatId).onMessageMarkRead(p.rid);
                pendingMessages.remove(p);
            }
        }
    }

    @Override
    public void markMessagesReceived(int chatType, int chatId, long date) {
        long uid = DialogUids.getDialogUid(chatType, chatId);
        Set<PendingMessage> pendingMessages = getPlainUnread(uid);
        for (PendingMessage p : pendingMessages.toArray(new PendingMessage[0])) {
            if (p.date <= date) {
                ConversationActor.conv(chatType, chatId).onMessageMarkReceived(p.rid);
                // pendingMessages.remove(p);
            }
        }
    }

    @Override
    public void markEncryptedRead(int chatType, int chatId, long rid) {
        ConversationActor.conv(chatType, chatId).onMessageMarkRead(rid);
    }

    @Override
    public void markEncryptedReceived(int chatType, int chatId, long rid) {
        ConversationActor.conv(chatType, chatId).onMessageMarkReceived(rid);
    }

    private Set<PendingMessage> getPlainUnread(long uid) {
        if (pendingCache.containsKey(uid)) {
            return pendingCache.get(uid);
        }
        BserMap<PendingMessage> map = new BserMap<PendingMessage>(new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()),
                "plain_remote_unread_" + uid), PendingMessage.class);
        PersistenceSet<PendingMessage> res = new PersistenceSet<PendingMessage>(map);
        pendingCache.put(uid, res);
        return res;
    }

    private static final int STATE_NONE = 0;
    private static final int STATE_RECEIVED = 1;
    private static final int STATE_READ = 2;

    public static class PendingMessage extends BserObject {
        private long rid;
        private long date;
        private int state;

        public PendingMessage(long rid, long date, int state) {
            this.rid = rid;
            this.date = date;
            this.state = state;
        }

        public PendingMessage() {
        }

        @Override
        public void parse(BserValues values) throws IOException {
            rid = values.getLong(1);
            date = values.getLong(2);
            state = values.getInt(3);
        }

        @Override
        public void serialize(BserWriter writer) throws IOException {
            writer.writeLong(1, rid);
            writer.writeLong(2, date);
            writer.writeInt(3, state);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PendingMessage that = (PendingMessage) o;

            if (rid != that.rid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (rid ^ (rid >>> 32));
        }
    }
}