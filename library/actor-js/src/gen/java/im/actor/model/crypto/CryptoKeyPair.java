package im.actor.model.crypto;

/**
 * Created by ex3ndr on 07.03.15.
 */
public class CryptoKeyPair {
    private byte[] publicKey;
    private byte[] privateKey;

    public CryptoKeyPair(byte[] publicKey, byte[] privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }
}
