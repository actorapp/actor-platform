package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.util.RandomUtils;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PrivateKeyStorage extends BserObject {

    private int keyGroupId;
    private PrivateKey identityKey = null;
    private PrivateKey[] keys;
    private PrivateKey[] preKeys;

    public PrivateKeyStorage(int keyGroupId, PrivateKey identityKey, PrivateKey[] keys, PrivateKey[] preKeys) {
        this.keyGroupId = keyGroupId;
        this.identityKey = identityKey;
        this.keys = keys;
        this.preKeys = preKeys;
    }

    public PrivateKeyStorage(byte[] data) throws IOException {
        load(data);
    }

    public int getKeyGroupId() {
        return keyGroupId;
    }

    public PrivateKey getIdentityKey() {
        return identityKey;
    }

    public PrivateKey[] getKeys() {
        return keys;
    }

    public PrivateKeyStorage setGroupId(int groupId) {
        return new PrivateKeyStorage(groupId, identityKey, keys, preKeys);
    }

    public PrivateKeyStorage appendPreKeys(PrivateKey[] addedPreKeys) {
        PrivateKey[] nKeys = new PrivateKey[
                addedPreKeys.length + preKeys.length];
        System.arraycopy(preKeys, 0, nKeys, 0, preKeys.length);
        System.arraycopy(addedPreKeys, 0, nKeys, preKeys.length, addedPreKeys.length);
        return new PrivateKeyStorage(keyGroupId, identityKey, keys, nKeys);
    }

    public PrivateKeyStorage markAsUploaded(PrivateKey[] uploadedPreKeys) {
        PrivateKey[] nKeys = new PrivateKey[preKeys.length];
        for (int i = 0; i < preKeys.length; i++) {
            boolean found = false;
            for (int j = 0; j < uploadedPreKeys.length; j++) {
                if (uploadedPreKeys[j].getKeyId() == preKeys[i].getKeyId()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                nKeys[i] = preKeys[i].markAsUploaded();
            } else {
                nKeys[i] = preKeys[i];
            }
        }
        return new PrivateKeyStorage(keyGroupId, identityKey, keys, nKeys);
    }

    public PrivateKey[] getPreKeys() {
        return preKeys;
    }

    public PrivateKey pickRandomPreKey() {
        ArrayList<PrivateKey> uploadedKeys = new ArrayList<PrivateKey>();
        for (PrivateKey u : preKeys) {
            if (u.isUploaded()) {
                uploadedKeys.add(u);
            }
        }
        return uploadedKeys.get(RandomUtils.randomId(uploadedKeys.size()));
    }

    @Override
    public void parse(BserValues values) throws IOException {
        keyGroupId = values.getInt(1);
        identityKey = new PrivateKey(values.getBytes(2));
        List<byte[]> r = values.getRepeatedBytes(3);
        keys = new PrivateKey[r.size()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new PrivateKey(r.get(i));
        }
        r = values.getRepeatedBytes(4);
        preKeys = new PrivateKey[r.size()];
        for (int i = 0; i < preKeys.length; i++) {
            preKeys[i] = new PrivateKey(r.get(i));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, keyGroupId);
        writer.writeBytes(2, identityKey.toByteArray());
        for (PrivateKey k : keys) {
            writer.writeBytes(3, k.toByteArray());
        }
        for (PrivateKey k : preKeys) {
            writer.writeBytes(4, k.toByteArray());
        }
    }
}
