package im.actor.crypto.primitives.digest;

import im.actor.crypto.primitives.Digest;

/**
 * Truncated SHA256 for public keys
 */
public class KeyDigest implements Digest {

    private SHA256 sha256 = new SHA256();

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
