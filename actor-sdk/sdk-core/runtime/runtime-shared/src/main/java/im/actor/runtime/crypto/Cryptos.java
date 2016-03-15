package im.actor.runtime.crypto;

import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.primitives.digest.CombinedHash;
import im.actor.runtime.crypto.primitives.digest.SHA512;
import im.actor.runtime.crypto.primitives.hmac.HMAC;
import im.actor.runtime.crypto.primitives.prf.PRF;
import im.actor.runtime.crypto.primitives.streebog.Streebog256;
import im.actor.runtime.crypto.primitives.streebog.Streebog512;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public final class Cryptos {

    public static HMAC HMAC_SHA256(byte[] secret) {
        return new HMAC(secret, Crypto.createSHA256());
    }

    public static HMAC HMAC_SHA512(byte[] secret) {
        return new HMAC(secret, new SHA512());
    }

    public static PRF PRF_SHA256() {
        return new PRF(Crypto.createSHA256());
    }

    public static PRF PRF_SHA512() {
        return new PRF(new SHA512());
    }

    public static PRF PRF_STREEBOG256() {
        return new PRF(new Streebog256());
    }

    public static PRF PRF_STREEBOG512() {
        return new PRF(new Streebog512());
    }

    public static PRF PRF_SHA_STREEBOG_256() {
        return new PRF(new CombinedHash(new Digest[]{
                Crypto.createSHA256(),
                new Streebog256()
        }));
    }

    private Cryptos() {

    }
}
