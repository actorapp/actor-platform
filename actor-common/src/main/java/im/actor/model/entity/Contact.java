package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserCreator;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.ListEngineItem;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class Contact extends BserObject implements ListEngineItem {

    public static Contact fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Contact(), data);
    }

    public static final BserCreator<Contact> CREATOR = new BserCreator<Contact>() {
        @Override
        public Contact createInstance() {
            return new Contact();
        }
    };

    private int uid;
    private long sortKey;
    private Avatar avatar;
    private String name;

    public Contact(int uid, long sortKey, Avatar avatar, String name) {
        this.uid = uid;
        this.sortKey = sortKey;
        this.avatar = avatar;
        this.name = name;
    }

    private Contact() {

    }

    public int getUid() {
        return uid;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        sortKey = values.getLong(2);
        name = values.getString(3);
        if (values.optBytes(4) != null) {
            avatar = Avatar.fromBytes(values.getBytes(4));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeLong(2, sortKey);
        writer.writeString(3, name);
        if (avatar != null) {
            writer.writeObject(4, avatar);
        }
    }

    @Override
    public long getEngineId() {
        return uid;
    }

    @Override
    public long getEngineSort() {
        return sortKey;
    }
}
