package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.modules.encryption.Configuration;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Crypto;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.crypto.Curve25519;

public class OwnKeys extends BserObject {

    private int keyGroupId;
    private OwnPrivateKey identityKey = null;
    private OwnPrivateKey[] keys;
    private OwnPrivateKeyUploadable[] ephemeralKeys;

    public OwnKeys(int keyGroupId, OwnPrivateKey identityKey, OwnPrivateKey[] keys, OwnPrivateKeyUploadable[] ephemeralKeys) {
        this.keyGroupId = keyGroupId;
        this.identityKey = identityKey;
        this.keys = keys;
        this.ephemeralKeys = ephemeralKeys;
    }

    public OwnKeys(byte[] data) throws IOException {
        load(data);
    }

    public int getKeyGroupId() {
        return keyGroupId;
    }

    public OwnPrivateKey getIdentityKey() {
        return identityKey;
    }

    public OwnPrivateKey[] getKeys() {
        return keys;
    }

    public OwnKeys setGroupId(int groupId) {
        return new OwnKeys(groupId, identityKey, keys, ephemeralKeys);
    }

    public OwnKeys appendEphemeralKeys(OwnPrivateKeyUploadable[] addedEphemeralKeys) {
        OwnPrivateKeyUploadable[] nKeys = new OwnPrivateKeyUploadable[
                addedEphemeralKeys.length + ephemeralKeys.length];
        System.arraycopy(ephemeralKeys, 0, nKeys, 0, ephemeralKeys.length);
        System.arraycopy(addedEphemeralKeys, 0, nKeys, ephemeralKeys.length, addedEphemeralKeys.length);
        return new OwnKeys(keyGroupId, identityKey, keys, nKeys);
    }

    public OwnKeys markAsUploaded(OwnPrivateKeyUploadable[] uploadedEphemeralKeys) {
        OwnPrivateKeyUploadable[] nKeys = new OwnPrivateKeyUploadable[ephemeralKeys.length];
        for (int i = 0; i < ephemeralKeys.length; i++) {
            boolean found = false;
            for (int j = 0; j < uploadedEphemeralKeys.length; j++) {
                if (uploadedEphemeralKeys[j].getKeyId() == ephemeralKeys[i].getKeyId()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                nKeys[i] = ephemeralKeys[i].markAsUploaded();
            } else {
                nKeys[i] = ephemeralKeys[i];
            }
        }
        return new OwnKeys(keyGroupId, identityKey, keys, nKeys);
    }

    public OwnPrivateKeyUploadable[] getEphemeralKeys() {
        return ephemeralKeys;
    }

    public OwnPrivateKeyUploadable pickRandomEphemeralKey() {
        ArrayList<OwnPrivateKeyUploadable> uploadedKeys = new ArrayList<OwnPrivateKeyUploadable>();
        for (OwnPrivateKeyUploadable u : ephemeralKeys) {
            if (u.isUploaded()) {
                uploadedKeys.add(u);
            }
        }
        return uploadedKeys.get(RandomUtils.randomId(uploadedKeys.size()));
    }

    @Override
    public void parse(BserValues values) throws IOException {
        keyGroupId = values.getInt(1);
        identityKey = new OwnPrivateKey(values.getBytes(2));
        List<byte[]> r = values.getRepeatedBytes(3);
        keys = new OwnPrivateKey[r.size()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new OwnPrivateKey(r.get(i));
        }
        r = values.getRepeatedBytes(4);
        ephemeralKeys = new OwnPrivateKeyUploadable[r.size()];
        for (int i = 0; i < ephemeralKeys.length; i++) {
            ephemeralKeys[i] = new OwnPrivateKeyUploadable(r.get(i));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, keyGroupId);
        writer.writeBytes(2, identityKey.toByteArray());
        for (OwnPrivateKey k : keys) {
            writer.writeBytes(3, k.toByteArray());
        }
        for (OwnPrivateKeyUploadable k : ephemeralKeys) {
            writer.writeBytes(4, k.toByteArray());
        }
    }
}
