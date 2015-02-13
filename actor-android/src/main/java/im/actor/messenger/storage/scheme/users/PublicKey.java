package im.actor.messenger.storage.scheme.users;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.keyvalue.KeyValueIdentity;

import java.io.IOException;

/**
 * Created by ex3ndr on 16.11.14.
 */
public class PublicKey extends BserObject implements KeyValueIdentity {
    private int uid;
    private long keyHash;
    private byte[] key;

    public PublicKey(int uid, long keyHash, byte[] key) {
        this.uid = uid;
        this.keyHash = keyHash;
        this.key = key;
    }

    public PublicKey() {
    }

    public int getUid() {
        return uid;
    }

    public long getKeyHash() {
        return keyHash;
    }

    public byte[] getKey() {
        return key;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        keyHash = values.getLong(2);
        key = values.getBytes(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeLong(2, keyHash);
        writer.writeBytes(3, key);
    }

    @Override
    public long getKeyValueId() {
        return keyHash;
    }
}
