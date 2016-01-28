package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class UserKeysGroup extends BserObject {

    private int keyGroupId;
    private PublicKey identityKey;
    private PublicKey[] keys;
    private PublicKey[] ephemeralKeys;

    public UserKeysGroup(int keyGroupId, PublicKey identityKey, PublicKey[] keys, PublicKey[] ephemeralKeys) {
        this.keyGroupId = keyGroupId;
        this.identityKey = identityKey;
        this.keys = keys;
        this.ephemeralKeys = ephemeralKeys;
    }

    public int getKeyGroupId() {
        return keyGroupId;
    }

    public PublicKey getIdentityKey() {
        return identityKey;
    }

    public PublicKey[] getKeys() {
        return keys;
    }

    public PublicKey[] getEphemeralKeys() {
        return ephemeralKeys;
    }

    public UserKeysGroup addPublicKey(PublicKey publicKey) {
        ArrayList<PublicKey> nEphemeralKeys = new ArrayList<PublicKey>();
        for (PublicKey p : this.ephemeralKeys) {
            if (p.getKeyId() != publicKey.getKeyId()) {
                nEphemeralKeys.add(p);
            }
        }
        nEphemeralKeys.add(publicKey);
        return new UserKeysGroup(keyGroupId, identityKey, keys, nEphemeralKeys.toArray(new PublicKey[nEphemeralKeys.size()]));
    }

    public UserKeysGroup(byte[] data) throws IOException {
        load(data);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        keyGroupId = values.getInt(1);
        identityKey = new PublicKey(values.getBytes(2));
        List<byte[]> r = values.getRepeatedBytes(3);
        keys = new PublicKey[r.size()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new PublicKey(r.get(i));
        }
        r = values.getRepeatedBytes(4);
        ephemeralKeys = new PublicKey[r.size()];
        for (int i = 0; i < ephemeralKeys.length; i++) {
            ephemeralKeys[i] = new PublicKey(r.get(i));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, keyGroupId);
        writer.writeBytes(2, identityKey.toByteArray());
        for (PublicKey k : keys) {
            writer.writeBytes(3, k.toByteArray());
        }
        for (PublicKey k : ephemeralKeys) {
            writer.writeBytes(4, k.toByteArray());
        }
    }
}
