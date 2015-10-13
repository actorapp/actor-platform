package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.KeyValueItem;

public class DialogDesc extends BserObject implements KeyValueItem {

    @Property("readonly, nonatomic")
    private Peer peer;
    @Property("readonly, nonatomic")
    private String title;
    @Property("readonly, nonatomic")
    private Avatar avatar;
    @Property("readonly, nonatomic")
    private boolean isUnread;
    @Property("readonly, nonatomic")
    private int counter;

    public DialogDesc(Peer peer, String title, Avatar avatar, boolean isUnread, int counter) {
        this.peer = peer;
        this.title = title;
        this.avatar = avatar;
        this.isUnread = isUnread;
        this.counter = counter;
    }

    public Peer getPeer() {
        return peer;
    }

    public String getTitle() {
        return title;
    }

    public Avatar getAvatar() {
        return avatar;
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
        title = values.getString(2);
        byte[] av = values.getBytes(3);
        if (av != null) {
            avatar = new Avatar(av);
        }
        counter = values.getInt(4);
        isUnread = values.getBool(5);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeObject(1, peer);
        writer.writeString(2, title);
        if (avatar != null) {
            writer.writeObject(3, avatar);
        }
        writer.writeInt(4, counter);
        writer.writeBool(5, isUnread);
    }

    @Override
    public long getEngineId() {
        return peer.getUnuqueId();
    }
}