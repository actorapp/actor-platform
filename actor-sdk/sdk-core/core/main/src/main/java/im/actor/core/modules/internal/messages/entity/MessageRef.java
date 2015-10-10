/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages.entity;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class MessageRef extends BserObject {
    private long rid;
    private long date;

    public MessageRef(long rid, long date) {
        this.rid = rid;
        this.date = date;
    }

    public MessageRef() {

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
