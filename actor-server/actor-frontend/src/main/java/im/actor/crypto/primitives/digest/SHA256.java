package im.actor.crypto.primitives.digest;

import im.actor.crypto.primitives.Digest;

public class SHA256 implements Digest {

    private SHA256Digest sha256Digest = new SHA256Digest();

    @Override
    public void reset() {
        sha256Digest.reset();
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        sha256Digest.update(src, offset, length);
    }

    @Override
    public void doFinal(byte[] dest, int destOffset) {
        sha256Digest.doFinal(dest, destOffset);
    }

    @Override
    public int getDigestSize() {
        return sha256Digest.getDigestSize();
    }
}