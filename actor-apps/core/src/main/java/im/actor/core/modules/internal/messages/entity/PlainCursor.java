/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages.entity;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.core.entity.Peer;

public class PlainCursor extends BserObject {

    public static PlainCursor fromBytes(byte[] data) throws IOException {
        return Bser.parse(new PlainCursor(), data);
    }

    private Peer peer;
    private long sortDate;
    private long pendingSortDate;

    public PlainCursor(Peer peer, long sortDate, long pendingSortDate) {
        this.peer = peer;
        this.sortDate = sortDate;
        this.pendingSortDate = pendingSortDate;
    }

    private PlainCursor() {

    }

    public Peer getPeer() {
        return peer;
    }

    public long getSortDate() {
        return sortDate;
    }

    public long getPendingSortDate() {
        return pendingSortDate;
    }

    public PlainCursor changeSortDate(long date) {
        return new PlainCursor(peer, date, pendingSortDate);
    }

    public PlainCursor changePendingSortDate(long pendingDate) {
        return new PlainCursor(peer, sortDate, pendingDate);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromUniqueId(values.getLong(1));
        sortDate = values.getLong(2);
        pendingSortDate = values.getLong(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, peer.getUnuqueId());
        writer.writeLong(2, sortDate);
        writer.writeLong(3, pendingSortDate);
    }
}
