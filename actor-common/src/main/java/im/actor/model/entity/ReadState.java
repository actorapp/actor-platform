package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.storage.KeyValueItem;

/**
 * Created by ex3ndr on 17.02.15.
 */
public class ReadState extends BserObject implements KeyValueItem {

    public static ReadState fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ReadState(), data);
    }

    private Peer peer;
    private long lastReadSortingDate;

    public ReadState(Peer peer, long lastReadSortingDate) {
        this.peer = peer;
        this.lastReadSortingDate = lastReadSortingDate;
    }

    private ReadState() {

    }

    public Peer getPeer() {
        return peer;
    }

    public long getLastReadSortingDate() {
        return lastReadSortingDate;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromUid(values.getLong(1));
        lastReadSortingDate = values.getLong(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, peer.getUid());
        writer.writeLong(2, lastReadSortingDate);
    }

    @Override
    public long getEngineId() {
        return peer.getUid();
    }
}
