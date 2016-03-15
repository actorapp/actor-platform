package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class fe_isnonzero {

//CONVERT #include "fe.h"
//CONVERT #include "crypto_verify_32.crypto_verify_32.h"

/*
return 1 if f == 0
return 0 if f != 0

Preconditions:
   |f| bounded by 1.1*2^26,1.1*2^25,1.1*2^26,1.1*2^25,etc.
*/

    static final byte[] zero = new byte[32];

    public static int fe_isnonzero(int[] f) {
        byte[] s = new byte[32];
        fe_tobytes.fe_tobytes(s, f);
        return crypto_verify_32.crypto_verify_32(s, zero);
    }


}
