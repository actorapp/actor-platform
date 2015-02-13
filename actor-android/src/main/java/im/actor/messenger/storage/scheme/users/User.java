package im.actor.messenger.storage.scheme.users;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.keyvalue.KeyValueIdentity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.messenger.storage.scheme.avatar.Avatar;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class User extends BserObject implements KeyValueIdentity {

    public static String name(String localName, String serverName) {
        if (localName == null || localName.trim().length() == 0) {
            return serverName;
        } else {
            return localName;
        }
    }

    private int id;
    private long accessHash;
    private String name;
    private String serverName;
    private String localName;
    private Sex sex;
    private List<Long> keyHashes;
    private long phone;
    private Avatar avatar;

    public User(int id, long accessHash, String serverName, String localName, Sex sex, List<Long> keyHashes, long phone, Avatar avatar) {
        this.id = id;
        this.accessHash = accessHash;
        this.serverName = serverName;
        this.name = name(localName, serverName);
        this.localName = localName;
        this.sex = sex;
        this.keyHashes = keyHashes;
        this.phone = phone;
        this.avatar = avatar;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public String getName() {
        return name;
    }

    public String getServerName() {
        return serverName;
    }

    public String getLocalName() {
        return localName;
    }

    public Sex getSex() {
        return sex;
    }

    public List<Long> getKeyHashes() {
        return keyHashes;
    }

    public long getPhone() {
        return phone;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public User changeLocalName(String localName) {
        return new User(id, accessHash, serverName, localName, sex, keyHashes, phone, avatar);
    }

    public User changeAvatar(Avatar avatar) {
        return new User(id, accessHash, serverName, localName, sex, keyHashes, phone, avatar);
    }

    public User changeServerName(String serverName) {
        return new User(id, accessHash, serverName, localName, sex, keyHashes, phone, avatar);
    }

    public User addKey(long key) {
        for (long k : keyHashes) {
            if (k == key) {
                return this;
            }
        }
        ArrayList<Long> nKeys = new ArrayList<Long>(keyHashes);
        nKeys.add(key);
        return new User(id, accessHash, serverName, localName, sex, nKeys, phone, avatar);
    }

    public User removeKey(long key) {
        ArrayList<Long> nKeys = new ArrayList<Long>(keyHashes);
        nKeys.remove(key);
        return new User(id, accessHash, serverName, localName, sex, nKeys, phone, avatar);
    }

    public User change(String serverName, String localName, Avatar avatar, Sex sex, List<Long> keyHashes) {
        return new User(id, accessHash, serverName, localName, sex, keyHashes, phone, avatar);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        id = values.getInt(1);
        accessHash = values.getLong(2);
        serverName = values.getString(4);
        localName = values.getString(5, null);
        name = name(localName, serverName);
        sex = Sex.parse(values.getInt(6));
        keyHashes = values.getRepeatedLong(7);
        phone = values.getLong(8);
        avatar = values.optObj(9, Avatar.class);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, id);
        writer.writeLong(2, accessHash);
        writer.writeString(4, serverName);
        if (localName != null) {
            writer.writeString(5, localName);
        }
        writer.writeInt(6, Sex.serialize(sex));
        writer.writeRepeatedLong(7, keyHashes);
        writer.writeLong(8, phone);
        if (avatar != null) {
            writer.writeObject(9, avatar);
        }
    }

    @Override
    public long getKeyValueId() {
        return id;
    }
}
