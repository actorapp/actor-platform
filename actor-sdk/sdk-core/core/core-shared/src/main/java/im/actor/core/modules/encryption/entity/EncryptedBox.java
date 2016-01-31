package im.actor.core.modules.encryption.entity;

public class EncryptedBox {

    private final EncryptedBoxKey[] keys;
    private final byte[] encryptedPackage;

    public EncryptedBox(EncryptedBoxKey[] keys, byte[] encryptedPackage) {
        this.keys = keys;
        this.encryptedPackage = encryptedPackage;
    }

    public EncryptedBoxKey[] getKeys() {
        return keys;
    }

    public byte[] getEncryptedPackage() {
        return encryptedPackage;
    }
}
