package im.actor.core.modules.messaging.actions.entity;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class DestructPendingMessage extends BserObject {

    public static DestructPendingMessage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new DestructPendingMessage(), data);
    }

    private long rid;
    private long date;
    private boolean isOut;
    private int timer;

    public DestructPendingMessage(long rid, long date, boolean isOut, int timer) {
        this.rid = rid;
        this.date = date;
        this.isOut = isOut;
        this.timer = timer;
    }

    private DestructPendingMessage() {

    }

    public long getRid() {
        return rid;
    }

    public long getDate() {
        return date;
    }

    public boolean isOut() {
        return isOut;
    }

    public int getTimer() {
        return timer;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        rid = values.getLong(1);
        date = values.getLong(2);
        isOut = values.getBool(3);
        timer = values.getInt(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, rid);
        writer.writeLong(2, date);
        writer.writeBool(3, isOut);
        writer.writeInt(4, timer);
    }
}
