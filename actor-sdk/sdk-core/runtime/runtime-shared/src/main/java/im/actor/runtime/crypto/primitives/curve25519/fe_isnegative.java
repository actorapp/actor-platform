package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class fe_isnegative {

//CONVERT #include "fe.h"

/*
return 1 if f is in {1,3,5,...,q-2}
return 0 if f is in {0,2,4,...,q-1}

Preconditions:
   |f| bounded by 1.1*2^26,1.1*2^25,1.1*2^26,1.1*2^25,etc.
*/

    public static int fe_isnegative(int[] f) {
        byte[] s = new byte[32];
        fe_tobytes.fe_tobytes(s, f);
        return s[0] & 1;
    }


}
