package im.actor.crypto.ratchet;

import im.actor.crypto.Curve25519;

public class RatchetPrivateKey extends RatchetPublicKey {

    private final byte[] privateKey;

    public RatchetPrivateKey(byte[] privateKey) {
        super(Curve25519.keyGenPublic(privateKey));

        this.privateKey = privateKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }
}
