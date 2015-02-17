package im.actor.model.modules.messages.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 11.02.15.
 */
public class PendingMessage extends BserObject {
    private long rid;
    private long date;

    public PendingMessage(long rid, long date) {
        this.rid = rid;
        this.date = date;
    }

    public PendingMessage() {

    }

    public long getDate() {
        return date;
    }

    public long getRid() {
        return rid;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        rid = values.getLong(1);
        date = values.getLong(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, rid);
        writer.writeLong(2, date);
    }
}
