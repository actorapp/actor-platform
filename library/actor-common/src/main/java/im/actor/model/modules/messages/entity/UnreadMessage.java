/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.Peer;

public class UnreadMessage extends BserObject {

    public static UnreadMessage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new UnreadMessage(), data);
    }

    private Peer peer;
    private long rid;
    private long sortDate;

    public UnreadMessage(Peer peer, long rid, long sortDate) {
        this.peer = peer;
        this.rid = rid;
        this.sortDate = sortDate;
    }

    public UnreadMessage() {

    }

    public Peer getPeer() {
        return peer;
    }

    public long getRid() {
        return rid;
    }

    public long getSortDate() {
        return sortDate;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromUniqueId(values.getLong(1));
        rid = values.getLong(2);
        sortDate = values.getLong(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, peer.getUnuqueId());
        writer.writeLong(2, rid);
        writer.writeLong(3, sortDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnreadMessage that = (UnreadMessage) o;

        if (rid != that.rid) return false;
        if (!peer.equals(that.peer)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = peer.hashCode();
        result = 31 * result + (int) (rid ^ (rid >>> 32));
        return result;
    }
}
