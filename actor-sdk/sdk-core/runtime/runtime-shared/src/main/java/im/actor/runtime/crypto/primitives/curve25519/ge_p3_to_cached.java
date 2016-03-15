package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ge_p3_to_cached {

//CONVERT #include "ge.h"

/*
r = p
*/

    static int[] d2 = {
//CONVERT #include "d2.h"
            -21827239, -5839606, -30745221, 13898782, 229458, 15978800, -12551817, -6495438, 29715968, 9444199
    };

    public static void ge_p3_to_cached(ge_cached r, ge_p3 p) {
        fe_add.fe_add(r.YplusX, p.Y, p.X);
        fe_sub.fe_sub(r.YminusX, p.Y, p.X);
        fe_copy.fe_copy(r.Z, p.Z);
        fe_mul.fe_mul(r.T2d, p.T, d2);
    }


}
