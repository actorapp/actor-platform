package im.actor.core.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.mvvm.ValueDefaultCreator;
import im.actor.runtime.storage.KeyValueItem;

public class EncryptedConversationState extends BserObject implements KeyValueItem {

    public static EncryptedConversationState fromBytes(byte[] data) throws IOException {
        return Bser.parse(new EncryptedConversationState(), data);
    }

    public static BserCreator<EncryptedConversationState> CREATOR = EncryptedConversationState::new;

    public static ValueDefaultCreator<EncryptedConversationState> DEFAULT_CREATOR = id ->
            new EncryptedConversationState((int) id, false, 0, 0, new ArrayList<>());

    private int uid;
    private boolean isLoaded;
    private int timer;
    private long timerDate;
    private ArrayList<ShortMessage> unreadMessages;


    public EncryptedConversationState(int uid, boolean isLoaded, int timer, long timerDate, ArrayList<ShortMessage> shortMessages) {
        this.uid = uid;
        this.isLoaded = isLoaded;
        this.timer = timer;
        this.timerDate = timerDate;
        this.unreadMessages = shortMessages;
    }

    private EncryptedConversationState() {
    }

    public int getUid() {
        return uid;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public int getTimer() {
        return timer;
    }

    public int getUnreadCount() {
        return unreadMessages.size();
    }

    public long getTimerDate() {
        return timerDate;
    }

    public EncryptedConversationState editTimer(int timer, long timerDate) {
        return new EncryptedConversationState(uid, isLoaded, timer, timerDate, unreadMessages);
    }

    public EncryptedConversationState addUnreadMessages(List<ShortMessage> messages) {
        ArrayList<ShortMessage> res = new ArrayList<>();
        res.addAll(this.unreadMessages);
        res.addAll(messages);
        return new EncryptedConversationState(uid, isLoaded, timer, timerDate, res);
    }

    public EncryptedConversationState readBefore(long sortDate) {
        ArrayList<ShortMessage> res = new ArrayList<>(unreadMessages);
        ShortMessage m;
        for (int i = res.size() - 1; i >= 0; i--) {
            m = res.get(i);
            if (m.getSortDate() <= sortDate) {
                res.remove(m);
            }
        }
        return new EncryptedConversationState(uid, isLoaded, timer, timerDate, res);
    }

    public EncryptedConversationState read(List<Long> ridsToRead) {
        ArrayList<ShortMessage> res = new ArrayList<>(unreadMessages);
        ArrayList<ShortMessage> rem = new ArrayList<>();
        for (Long rid : ridsToRead) {
            rem.add(new ShortMessage(0, rid));
        }
        res.removeAll(rem);
        return new EncryptedConversationState(uid, isLoaded, timer, timerDate, res);
    }

    public EncryptedConversationState readAll() {
        ArrayList<ShortMessage> res = new ArrayList<>();
        return new EncryptedConversationState(uid, isLoaded, timer, timerDate, res);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        isLoaded = values.getBool(2);
        timer = values.getInt(3);
        timerDate = values.getLong(4);
        unreadMessages = new ArrayList<>();
        for (byte[] i : values.getRepeatedBytes(5)) {
            ShortMessage m = new ShortMessage();
            Bser.parse(m, i);
            unreadMessages.add(m);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeBool(2, isLoaded);
        writer.writeLong(3, timer);
        writer.writeLong(4, timerDate);
        writer.writeRepeatedObj(5, unreadMessages);
    }

    @Override
    public long getEngineId() {
        return uid;
    }

    public static class ShortMessage extends BserObject {
        private long sortDate;
        private long rid;

        public ShortMessage() {
        }

        public ShortMessage(long sortDate, long rid) {
            this.sortDate = sortDate;
            this.rid = rid;
        }

        public long getSortDate() {
            return sortDate;
        }

        public long getRid() {
            return rid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ShortMessage that = (ShortMessage) o;

            return sortDate == that.sortDate || rid == that.rid;
        }

        @Override
        public void parse(BserValues values) throws IOException {
            sortDate = values.getLong(1);
            rid = values.getLong(2);
        }

        @Override
        public void serialize(BserWriter writer) throws IOException {
            writer.writeLong(1, sortDate);
            writer.writeLong(2, rid);
        }
    }
}
