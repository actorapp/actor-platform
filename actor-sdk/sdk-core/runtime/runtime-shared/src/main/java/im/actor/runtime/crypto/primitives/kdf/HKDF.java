package im.actor.runtime.crypto.primitives.kdf;

import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.primitives.hmac.HMAC;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

/**
 * HKDF implementation based on RFC 5869: https://tools.ietf.org/html/rfc5869
 *
 * @author Steve Kite (steve@actor.im)
 */
public class HKDF {

    private Digest baseDigest;

    public HKDF(Digest baseDigest) {
        this.baseDigest = baseDigest;
    }

    public byte[] deriveSecrets(byte[] keyMaterial, int outputLength) {
        return deriveSecrets(keyMaterial, new byte[0], outputLength);
    }

    public byte[] deriveSecrets(byte[] keyMaterial, byte[] info, int outputLength) {
        return deriveSecrets(keyMaterial, new byte[baseDigest.getDigestSize()], info, outputLength);
    }

    public byte[] deriveSecrets(byte[] keyMaterial, byte[] salt, byte[] info, int outputLength) {
        byte[] prk = hkdfExtract(salt, keyMaterial);
        return hkdfExpand(prk, info, outputLength);
    }

    byte[] hkdfExtract(byte[] keyMaterial, byte[] salt) {
        HMAC hmac = new HMAC(salt, baseDigest);
        hmac.reset();
        hmac.update(keyMaterial, 0, keyMaterial.length);
        byte[] res = new byte[baseDigest.getDigestSize()];
        hmac.doFinal(res, 0);
        return res;
    }

    byte[] hkdfExpand(byte[] prk, byte[] info, int outputSize) {
        byte[] res = new byte[outputSize];
        HMAC hmac = new HMAC(prk, baseDigest);
        hmac.reset();
        byte[] prevHash = new byte[0];
        int offset = 0;
        int index = 0;
        byte[] indexB = new byte[1];
        while (offset < res.length) {
            hmac.reset();
            hmac.update(prevHash, 0, prevHash.length);
            hmac.update(info, 0, info.length);
            indexB[0] = (byte) index;
            hmac.update(indexB, 0, 1);

            byte[] result = new byte[baseDigest.getDigestSize()];
            hmac.doFinal(result, 0);

            int digestSize = baseDigest.getDigestSize();
            int blockLength = Math.min(outputSize - offset, digestSize);
            ByteStrings.write(res, offset, result, 0, blockLength);

            prevHash = result;

            offset += digestSize;
            index++;
        }

        return res;
    }
}
