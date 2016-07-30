package im.actor.core.modules.messaging.actions.entity;

import java.io.IOException;

import im.actor.core.entity.Peer;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class DestructQueueMessage extends BserObject {

    public static DestructQueueMessage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new DestructQueueMessage(), data);
    }

    private Peer peer;
    private long rid;
    private long destructDate;

    public DestructQueueMessage(Peer peer, long rid, long destructDate) {
        this.peer = peer;
        this.rid = rid;
        this.destructDate = destructDate;
    }

    private DestructQueueMessage() {

    }

    public Peer getPeer() {
        return peer;
    }

    public long getRid() {
        return rid;
    }

    public long getDestructDate() {
        return destructDate;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromUniqueId(values.getLong(1));
        rid = values.getLong(2);
        destructDate = values.getLong(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, peer.getUnuqueId());
        writer.writeLong(2, rid);
        writer.writeLong(3, destructDate);
    }
}
