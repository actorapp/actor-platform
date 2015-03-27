package im.actor.model.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.KeyValueItem;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class User extends BserObject implements KeyValueItem {

    public static User fromBytes(byte[] data) throws IOException {
        return Bser.parse(new User(), data);
    }

    private int uid;
    private long accessHash;
    private String name;
    private String localName;
    private Avatar avatar;
    private Sex sex;
    private List<ContactRecord> records;

    public User(int uid, long accessHash, String name, String localName,
                Avatar avatar, Sex sex, List<ContactRecord> records) {
        this.uid = uid;
        this.accessHash = accessHash;
        this.name = name;
        this.localName = localName;
        this.avatar = avatar;
        this.sex = sex;
        this.records = records;
    }

    private User() {

    }

    public Peer peer() {
        return new Peer(PeerType.PRIVATE, uid);
    }

    public int getUid() {
        return uid;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public String getServerName() {
        return name;
    }

    public String getLocalName() {
        return localName;
    }

    public String getName() {
        if (localName == null) {
            return name;
        } else {
            return localName;
        }
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public Sex getSex() {
        return sex;
    }

    public List<ContactRecord> getRecords() {
        return records;
    }

    public User editName(String name) {
        return new User(uid, accessHash, name, localName, avatar, sex, records);
    }

    public User editLocalName(String localName) {
        return new User(uid, accessHash, name, localName, avatar, sex, records);
    }

    public User editAvatar(Avatar avatar) {
        return new User(uid, accessHash, name, localName, avatar, sex, records);
    }

    @Override
    public long getEngineId() {
        return getUid();
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        accessHash = values.getLong(2);
        name = values.getString(3);
        localName = values.optString(4);
        byte[] a = values.optBytes(5);
        if (a != null) {
            avatar = Avatar.fromBytes(a);
        }
        sex = Sex.fromValue(values.getInt(6));
        int count = values.getRepeatedCount(7);
        if (count > 0) {
            ArrayList<ContactRecord> rec = new ArrayList<ContactRecord>();
            for (int i = 0; i < count; i++) {
                rec.add(new ContactRecord());
            }
            records = values.getRepeatedObj(7, rec);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeLong(2, accessHash);
        writer.writeString(3, name);
        if (localName != null) {
            writer.writeString(4, localName);
        }
        if (avatar != null) {
            writer.writeObject(5, avatar);
        }
        writer.writeInt(6, sex.getValue());
        writer.writeRepeatedObj(7, records);
    }
}