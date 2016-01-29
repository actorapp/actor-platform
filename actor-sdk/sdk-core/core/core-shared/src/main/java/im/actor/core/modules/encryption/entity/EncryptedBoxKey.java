package im.actor.core.modules.encryption.entity;

import im.actor.runtime.function.Predicate;

public class EncryptedBoxKey {

    public static Predicate<EncryptedBoxKey> FILTER(final int myUid, final int keyGroupId) {
        return new Predicate<EncryptedBoxKey>() {
            @Override
            public boolean apply(EncryptedBoxKey boxKey) {
                return boxKey.getUid() == myUid && boxKey.getKeyGroupId() == keyGroupId;
            }
        };
    }

    private final int uid;
    private final int keyGroupId;
    private final byte[] encryptedKey;
    private final String keyAlg;

    public EncryptedBoxKey(int uid, int keyGroupId, String keyAlg, byte[] encryptedKey) {
        this.uid = uid;
        this.keyGroupId = keyGroupId;
        this.encryptedKey = encryptedKey;
        this.keyAlg = keyAlg;
    }

    public String getKeyAlg() {
        return keyAlg;
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
