package im.actor.core.modules.encryption.entity;

import java.util.Arrays;

import im.actor.runtime.crypto.Curve25519;

public class EphemeralKey {

    private final byte[] privateKey;
    private final byte[] publicKey;

    public EphemeralKey(byte[] privateKey) {
        this.privateKey = privateKey;
        this.publicKey = Curve25519.keyGenPublic(privateKey);
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void safeErase() {
        for (int i = 0; i < privateKey.length; i++) {
            privateKey[i] = 0;
        }
        for (int i = 0; i < publicKey.length; i++) {
            publicKey[i] = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EphemeralKey that = (EphemeralKey) o;

        if (!Arrays.equals(privateKey, that.privateKey)) return false;
        return Arrays.equals(publicKey, that.publicKey);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(privateKey);
        result = 31 * result + Arrays.hashCode(publicKey);
        return result;
    }
}
