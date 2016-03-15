package im.actor.runtime.crypto.primitives.digest;

import im.actor.runtime.crypto.Digest;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class SHA512 implements Digest {

    private SHA512Digest sha512Digest = new SHA512Digest();

    @Override
    public void reset() {
        sha512Digest.reset();
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        sha512Digest.update(src, offset, length);
    }

    @Override
    public void doFinal(byte[] dest, int destOffset) {
        sha512Digest.doFinal(dest, destOffset);
    }

    @Override
    public int getDigestSize() {
        return sha512Digest.getDigestSize();
    }
}
