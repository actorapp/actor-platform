package im.actor.runtime.crypto.primitives.digest;

import im.actor.runtime.crypto.Digest;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class MD5 implements Digest {

    private static final int DIGEST_LENGTH = 16;

    private MD5Digest md5Digest = new MD5Digest();

    @Override
    public void reset() {
        md5Digest.reset();
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        md5Digest.update(src, offset, length);
    }

    @Override
    public void doFinal(byte[] dest, int destOffset) {
        md5Digest.doFinal(dest, destOffset);
    }

    @Override
    public int getDigestSize() {
        return DIGEST_LENGTH;
    }
}
