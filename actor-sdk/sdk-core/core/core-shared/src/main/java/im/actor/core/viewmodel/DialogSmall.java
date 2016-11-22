package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import java.io.IOException;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.Peer;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.ListEngineItem;

public class DialogSmall extends BserObject implements ListEngineItem{

    @Property("readonly, nonatomic")
    private Peer peer;
    @Property("readonly, nonatomic")
    private String title;
    @Property("readonly, nonatomic")
    private Avatar avatar;
    @Property("readonly, nonatomic")
    private int counter;

    public DialogSmall(Peer peer, String title, Avatar avatar, int counter) {
        this.peer = peer;
        this.title = title;
        this.avatar = avatar;
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

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromBytes(values.getBytes(1));
        title = values.getString(2);
        avatar = new Avatar(values.getBytes(3));
        counter = values.getInt(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBytes(1, peer.toByteArray());
        writer.writeString(2, title);
        writer.writeBytes(3, avatar.toByteArray());
        writer.writeInt(4, counter);
    }

    @Override
    public long getEngineId() {
        return peer.getUnuqueId();
    }

    @Override
    public long getEngineSort() {
        return peer.getUnuqueId();
    }

    @Override
    public String getEngineSearch() {
        return null;
    }
}
