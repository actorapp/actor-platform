/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.notifications.entity;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.Peer;

public class PendingNotification extends BserObject {
    private Peer peer;
    private int sender;
    private long date;
    private ContentDescription content;

    public PendingNotification(Peer peer, int sender, long date, ContentDescription content) {
        this.peer = peer;
        this.sender = sender;
        this.date = date;
        this.content = content;
    }

    public PendingNotification() {
    }

    public Peer getPeer() {
        return peer;
    }

    public int getSender() {
        return sender;
    }

    public long getDate() {
        return date;
    }

    public ContentDescription getContent() {
        return content;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromUniqueId(values.getLong(1));
        sender = values.getInt(2);
        date = values.getLong(4);
        content = ContentDescription.fromBytes(values.getBytes(5));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, peer.getUnuqueId());
        writer.writeInt(2, sender);
        writer.writeLong(4, date);
        writer.writeObject(5, content);
    }
}
