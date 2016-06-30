package im.actor.runtime.crypto.ratchet;

import im.actor.runtime.crypto.Curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

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
