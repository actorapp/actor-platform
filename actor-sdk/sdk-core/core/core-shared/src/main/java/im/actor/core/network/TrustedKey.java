package im.actor.core.network;

import im.actor.runtime.crypto.primitives.digest.SHA256;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

public class TrustedKey {

    private final long keyId;
    private final byte[] key;

    public TrustedKey(byte[] key) {
        byte[] hash = new byte[32];
        SHA256 sha256 = new SHA256();
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
