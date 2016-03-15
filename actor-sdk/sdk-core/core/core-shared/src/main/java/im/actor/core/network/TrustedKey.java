package im.actor.core.network;

import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class TrustedKey {

    private final long keyId;
    private final byte[] key;

    public TrustedKey(byte[] key) {
        byte[] hash = new byte[32];
        Digest sha256 = Crypto.createSHA256();
        sha256.update(key, 0, key.length);
        sha256.doFinal(hash, 0);
        this.keyId = ByteStrings.bytesToLong(hash);
        this.key = key;
    }

    public long getKeyId() {
        return keyId;
    }

    public byte[] getKey() {
        return key;
    }
}
