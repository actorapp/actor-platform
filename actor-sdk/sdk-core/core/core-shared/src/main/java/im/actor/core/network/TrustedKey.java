package im.actor.core.network;

import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.util.Hex;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class TrustedKey {

    private boolean isLoaded = false;
    private final String hexKey;
    private long keyId;
    private byte[] key;

    public TrustedKey(String hexKey) {
        this.hexKey = hexKey;
    }

    public long getKeyId() {
        load();
        return keyId;
    }

    public byte[] getKey() {
        load();
        return key;
    }

    private synchronized void load() {
        if (!isLoaded) {
            isLoaded = true;

            this.key = Hex.fromHex(hexKey);

            byte[] hash = new byte[32];
            Digest sha256 = Crypto.createSHA256();
            sha256.update(key, 0, key.length);
            sha256.doFinal(hash, 0);
            this.keyId = ByteStrings.bytesToLong(hash);
        }
    }
}
