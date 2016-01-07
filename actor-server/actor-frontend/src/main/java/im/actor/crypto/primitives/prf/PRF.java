package im.actor.crypto.primitives.prf;

import im.actor.crypto.primitives.Digest;
import im.actor.crypto.primitives.hmac.HMAC;
import im.actor.crypto.primitives.util.ByteStrings;

public class PRF {

    private Digest digest;
    private HMAC hmac;
    private String label;
    private int length;

    public PRF(Digest digest, String label, int length) {
        this.digest = digest;
        this.label = label;
        this.length = length;
        this.hmac = new HMAC(digest);
    }

    public byte[] calculate(byte[] secret, byte[] seed) {
        // PRF(secret: bytes, label: string, seed: bytes) = P_HASH(secret, bytes(label) + seed);
        // P_HASH(secret, seed) = HASH(secret, A(1) + seed) + HASH(secret, A(2) + seed) + HASH(secret, A(3) + seed) + ...
        //    where A():
        //    A(0) = seed
        //    A(i) = HMAC_HASH(secret, A(i-1))
        byte[] rSeed = ByteStrings.merge(label.getBytes(), seed);
        byte[] res = new byte[length];
        byte[] A = rSeed;
        byte[] tHash = new byte[digest.getDigestSize()];
        int offset = 0;
        while (offset * 32 < length) {

            // Update A
            hmac.calculate(secret, A, 0, A.length, tHash, 0);
            A = new byte[digest.getDigestSize()];
            ByteStrings.write(A, 0, tHash, 0, A.length);

            // Writing digest
            digest.reset();
            digest.update(secret, 0, secret.length);
            digest.update(A, 0, A.length);
            digest.update(rSeed, 0, rSeed.length);
            digest.doFinal(tHash, 0);

            ByteStrings.write(res, offset * digest.getDigestSize(), tHash, 0, Math.min(tHash.length, res.length - offset * digest.getDigestSize()));
            offset++;
        }
        return res;
    }
}