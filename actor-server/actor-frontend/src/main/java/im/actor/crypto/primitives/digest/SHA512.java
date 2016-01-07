package im.actor.crypto.primitives.digest;

import im.actor.crypto.primitives.Digest;

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
