package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.KeyValueItem;

public class DialogSpec extends BserObject implements KeyValueItem {

    public static BserCreator<DialogSpec> CREATOR = new BserCreator<DialogSpec>() {
        @Override
        public DialogSpec createInstance() {
            return new DialogSpec();
        }
    };

    @Property("readonly, nonatomic")
    private Peer peer;
    @Property("readonly, nonatomic")
    private boolean isUnread;
    @Property("readonly, nonatomic")
    private int counter;

    public DialogSpec(Peer peer, boolean isUnread, int counter) {
        this.peer = peer;
        this.isUnread = isUnread;
        this.counter = counter;
    }

    private DialogSpec() {

    }

    public Peer getPeer() {
        return peer;
    }

    public int getCounter() {
        return counter;
    }

    public boolean isUnread() {
        return isUnread;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromBytes(values.getBytes(1));
        counter = values.getInt(2);
        isUnread = values.getBool(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeObject(1, peer);
        writer.writeInt(2, counter);
        writer.writeBool(3, isUnread);
    }

    @Override
    public long getEngineId() {
        return peer.getUnuqueId();
    }
}