package im.actor.runtime.crypto.primitives.digest;

import im.actor.runtime.crypto.Digest;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class SHA256 implements Digest {

    private SHA256Digest sha256Digest = new SHA256Digest();

    @Override
    public void reset() {
        sha256Digest.reset();
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        if (length + offset > src.length) {
            throw new RuntimeException("Incorrect length");
        }
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