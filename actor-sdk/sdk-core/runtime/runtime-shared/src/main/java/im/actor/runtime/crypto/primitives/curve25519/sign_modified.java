package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class sign_modified {

//CONVERT #include <string.h>
//CONVERT #include "crypto_sign.h"
//CONVERT #include "crypto_hash_sha512.h"
//CONVERT #include "ge.h"
//CONVERT #include "sc.h"
//CONVERT #include "zeroize.h"

    /* NEW: Compare to pristine crypto_sign()
       Uses explicit private key for nonce derivation and as scalar,
       instead of deriving both from a master key.
    */
    static int crypto_sign_modified(
            Sha512 sha512provider,
            byte[] sm,
            byte[] m, long mlen,
            byte[] sk, byte[] pk,
            byte[] random
    ) {
        byte[] nonce = new byte[64];
        byte[] hram = new byte[64];
        ge_p3 R = new ge_p3();
        int count = 0;

        System.arraycopy(m, 0, sm, 64, (int) mlen);
        System.arraycopy(sk, 0, sm, 32, 32);

  /* NEW : add prefix to separate hash uses - see .h */
        sm[0] = (byte) 0xFE;
        for (count = 1; count < 32; count++)
            sm[count] = (byte) 0xFF;

  /* NEW: add suffix of random data */
        System.arraycopy(random, 0, sm, (int) (mlen + 64), 64);

        sha512provider.calculateDigest(nonce, sm, mlen + 128);
        System.arraycopy(pk, 0, sm, 32, 32);

        sc_reduce.sc_reduce(nonce);
        ge_scalarmult_base.ge_scalarmult_base(R, nonce);
        ge_p3_tobytes.ge_p3_tobytes(sm, R);

        sha512provider.calculateDigest(hram, sm, mlen + 64);
        sc_reduce.sc_reduce(hram);
        byte[] S = new byte[32];
        sc_muladd.sc_muladd(S, hram, sk, nonce); /* NEW: Use privkey directly */
        System.arraycopy(S, 0, sm, 32, 32);

        return 0;
    }


}
