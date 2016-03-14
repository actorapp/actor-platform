package im.actor.runtime.crypto.primitives.digest;

import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.Digest;

/**
 * Truncated SHA256 for public keys
 */
public class KeyDigest implements Digest {

    private Digest sha256 = Crypto.createSHA256();

    @Override
    public void reset() {
        sha256.reset();
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        sha256.update(src, offset, length);
    }

    @Override
    public void doFinal(byte[] dest, int destOffset) {
        byte[] res = new byte[32];
        sha256.doFinal(res, 0);
        System.arraycopy(res, 0, dest, 0, 8);
    }

    @Override
    public int getDigestSize() {
        return 8;
    }
}
