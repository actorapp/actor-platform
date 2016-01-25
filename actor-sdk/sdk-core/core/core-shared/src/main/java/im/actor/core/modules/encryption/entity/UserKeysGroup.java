package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class UserKeysGroup extends BserObject {

    private int keyGroupId;
    private UserPublicKey identityKey;
    private UserPublicKey[] keys;
    private UserPublicKey[] ephemeralKeys;

    public UserKeysGroup(int keyGroupId, UserPublicKey identityKey, UserPublicKey[] keys, UserPublicKey[] ephemeralKeys) {
        this.keyGroupId = keyGroupId;
        this.identityKey = identityKey;
        this.keys = keys;
        this.ephemeralKeys = ephemeralKeys;
    }

    public int getKeyGroupId() {
        return keyGroupId;
    }

    public UserPublicKey getIdentityKey() {
        return identityKey;
    }

    public UserPublicKey[] getKeys() {
        return keys;
    }

    public UserPublicKey[] getEphemeralKeys() {
        return ephemeralKeys;
    }

    public UserKeysGroup addUserKeyGroup(UserPublicKey publicKey) {
        ArrayList<UserPublicKey> nEphemeralKeys = new ArrayList<UserPublicKey>();
        for (UserPublicKey p : ephemeralKeys) {
            if (p.getKeyId() != publicKey.getKeyId()) {
                nEphemeralKeys.add(p);
            }
        }
        nEphemeralKeys.add(publicKey);
        return new UserKeysGroup(keyGroupId, identityKey, keys, nEphemeralKeys.toArray(new UserPublicKey[nEphemeralKeys.size()]));
    }

    public UserKeysGroup(byte[] data) throws IOException {
        load(data);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        keyGroupId = values.getInt(1);
        identityKey = new UserPublicKey(values.getBytes(2));
        List<byte[]> r = values.getRepeatedBytes(3);
        keys = new UserPublicKey[r.size()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new UserPublicKey(r.get(i));
        }
        r = values.getRepeatedBytes(4);
        ephemeralKeys = new UserPublicKey[r.size()];
        for (int i = 0; i < ephemeralKeys.length; i++) {
            ephemeralKeys[i] = new UserPublicKey(r.get(i));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, keyGroupId);
        writer.writeBytes(2, identityKey.toByteArray());
        for (UserPublicKey k : keys) {
            writer.writeBytes(3, k.toByteArray());
        }
        for (UserPublicKey k : ephemeralKeys) {
            writer.writeBytes(4, k.toByteArray());
        }
    }
}
