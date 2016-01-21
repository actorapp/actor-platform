package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PrivateKeyStorage extends BserObject {

    private int keyGroupId;
    private EncryptionKey identityKey = null;
    private ArrayList<EncryptionKey> keys = new ArrayList<EncryptionKey>();

    public PrivateKeyStorage(EncryptionKey identityKey, ArrayList<EncryptionKey> keys, int keyGroupId) {
        this.identityKey = identityKey;
        this.keys = keys;
        this.keyGroupId = keyGroupId;
    }

    public PrivateKeyStorage(byte[] data) throws IOException {
        load(data);
    }

    public EncryptionKey getIdentityKey() {
        return identityKey;
    }

    public ArrayList<EncryptionKey> getKeys() {
        return keys;
    }

    public int getKeyGroupId() {
        return keyGroupId;
    }

    public PrivateKeyStorage markUploaded(int keyGroupId) {
        return new PrivateKeyStorage(identityKey, keys, keyGroupId);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        identityKey = new EncryptionKey(values.getBytes(1));
        keyGroupId = values.optInt(2);
        for (byte[] b : values.getRepeatedBytes(3)) {
            keys.add(new EncryptionKey(b));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeObject(1, identityKey);
        writer.writeInt(2, keyGroupId);
        writer.writeRepeatedObj(3, keys);
    }
}
