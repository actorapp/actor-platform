package im.actor.crypto;

public class Curve25519KeyPair {

    private byte[] publicKey;
    private byte[] privateKey;

    public Curve25519KeyPair(byte[] publicKey, byte[] privateKey) {
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