package im.actor.core.modules.encryption.entity;

public class EncryptedBoxKey {

    private final int uid;
    private final int keyGroupId;
    private final byte[] encryptedKey;

    public EncryptedBoxKey(int uid, int keyGroupId, byte[] encryptedKey) {
        this.uid = uid;
        this.keyGroupId = keyGroupId;
        this.encryptedKey = encryptedKey;
    }

    public int getUid() {
        return uid;
    }

    public int getKeyGroupId() {
        return keyGroupId;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }
}
