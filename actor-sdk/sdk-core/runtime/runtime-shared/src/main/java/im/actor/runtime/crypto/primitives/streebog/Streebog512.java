package im.actor.runtime.crypto.primitives.streebog;

import im.actor.runtime.crypto.Digest;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ALL_CHECKS 1
]-*/

public class Streebog512 implements Digest {

    private static final int DIGEST_SIZE = 64;

    private StreebogFastDigest streebogDigest = new StreebogFastDigest(DIGEST_SIZE);

    @Override
    public void reset() {
        streebogDigest.reset();
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        streebogDigest.update(src, offset, length);
    }

    @Override
    public void doFinal(byte[] dest, int destOffset) {
        streebogDigest.doFinal(dest, destOffset);
    }

    @Override
    public int getDigestSize() {
        return DIGEST_SIZE;
    }
}
