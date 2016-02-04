package im.actor.core.modules.encryption.entity;

public class EncryptedBox {
    private final int senderKeyGroupId;
    private final EncryptedBoxKey[] keys;
    private final byte[] encryptedPackage;

    public EncryptedBox(int senderKeyGroupId, EncryptedBoxKey[] keys, byte[] encryptedPackage) {
        this.senderKeyGroupId = senderKeyGroupId;
        this.keys = keys;
        this.encryptedPackage = encryptedPackage;
    }

    public int getSenderKeyGroupId() {
        return senderKeyGroupId;
    }

    public EncryptedBoxKey[] getKeys() {
        return keys;
    }

    public byte[] getEncryptedPackage() {
        return encryptedPackage;
    }
}
