package im.actor.crypto.primitives.streebog;

import im.actor.crypto.primitives.Digest;

public class Streebog256 implements Digest {

    private static final int DIGEST_SIZE = 32;

    private StreebogDigest streebogDigest = new StreebogDigest(DIGEST_SIZE);

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
