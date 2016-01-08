package im.actor.crypto.primitives.digest;

import im.actor.crypto.primitives.Digest;

public class CombinedHash implements Digest {

    private Digest[] digests;

    public CombinedHash(Digest[] digests) {
        this.digests = digests;
    }

    @Override
    public void reset() {
        for (int i = 0; i < digests.length; i++) {
            digests[i].reset();
        }
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        for (int i = 0; i < digests.length; i++) {
            digests[i].update(src, offset, length);
        }
    }

    @Override
    public void doFinal(byte[] dest, int destOffset) {
        // Folding digests
        for (int i = digests.length - 1; i > 0; i--) {
            byte[] currentDigest = new byte[digests[i].getDigestSize()];
            digests[i].doFinal(currentDigest, 0);
            digests[i - 1].update(currentDigest, 0, currentDigest.length);
        }
        digests[0].doFinal(dest, destOffset);

        reset();
    }

    @Override
    public int getDigestSize() {
        // Outer Hash
        return digests[0].getDigestSize();
    }
}
