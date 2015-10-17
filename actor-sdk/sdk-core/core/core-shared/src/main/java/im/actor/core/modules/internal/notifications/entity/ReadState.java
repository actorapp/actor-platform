package im.actor.core.modules.internal.notifications.entity;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class ReadState extends BserObject {

    public static ReadState fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ReadState(), data);
    }

    private long sortDate;

    private ReadState() {

    }

    public ReadState(long sortDate) {
        this.sortDate = sortDate;
    }

    public long getSortDate() {
        return sortDate;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.sortDate = values.getLong(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, sortDate);
    }
}
