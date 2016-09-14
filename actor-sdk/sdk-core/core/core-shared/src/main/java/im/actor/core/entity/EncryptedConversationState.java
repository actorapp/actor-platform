package im.actor.core.entity;

import java.io.IOException;
import java.util.ArrayList;

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
    private ArrayList<Long> unreadMessagesRids;


    public EncryptedConversationState(int uid, boolean isLoaded, int timer, long timerDate, ArrayList<Long> unreadMessagesRids) {
        this.uid = uid;
        this.isLoaded = isLoaded;
        this.timer = timer;
        this.timerDate = timerDate;
        this.unreadMessagesRids = unreadMessagesRids;
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
        return unreadMessagesRids.size();
    }

    public long getTimerDate() {
        return timerDate;
    }

    public EncryptedConversationState editTimer(int timer, long timerDate) {
        return new EncryptedConversationState(uid, isLoaded, timer, timerDate, unreadMessagesRids);
    }

    public EncryptedConversationState addUnreadMessages(ArrayList<Long> rids) {
        ArrayList<Long> res = new ArrayList<>();
        res.addAll(unreadMessagesRids);
        res.addAll(rids);
        return new EncryptedConversationState(uid, isLoaded, timer, timerDate, res);
    }

    public EncryptedConversationState readAll() {
        ArrayList<Long> res = new ArrayList<>();
        return new EncryptedConversationState(uid, isLoaded, timer, timerDate, res);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        isLoaded = values.getBool(2);
        timer = values.getInt(3);
        timerDate = values.getLong(4);
        unreadMessagesRids = new ArrayList<>();
        for (long i : values.getRepeatedLong(5)) {
            unreadMessagesRids.add(i);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeBool(2, isLoaded);
        writer.writeLong(3, timer);
        writer.writeLong(4, timerDate);
        writer.writeRepeatedLong(5, unreadMessagesRids);
    }

    @Override
    public long getEngineId() {
        return uid;
    }
}
