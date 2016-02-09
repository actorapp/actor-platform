package im.actor.crypto.primitives.digest;

import im.actor.crypto.primitives.Digest;

public class DigestWrapper implements Digest {

    private Digest baseDigest;

    public DigestWrapper(Digest baseDigest) {
        this.baseDigest = baseDigest;
    }

    @Override
    public void reset() {
        baseDigest.reset();
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        baseDigest.update(src, offset, length);
    }

    @Override
    public void doFinal(byte[] dest, int destOffset) {
        baseDigest.doFinal(dest, destOffset);
    }

    public byte[] digest(byte[] src) {
        return digest(src, 0, src.length);
    }

    public byte[] digest(byte[] src, int offset, int length) {
        baseDigest.reset();
        baseDigest.update(src, offset, length);
        byte[] res = new byte[getDigestSize()];
        baseDigest.doFinal(res, 0);
        return res;
    }

    @Override
    public int getDigestSize() {
        return baseDigest.getDigestSize();
    }
}
