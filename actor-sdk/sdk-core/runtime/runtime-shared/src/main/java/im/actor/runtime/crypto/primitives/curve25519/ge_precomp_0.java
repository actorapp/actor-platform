package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ge_precomp_0 {

//CONVERT #include "ge.h"

    public static void ge_precomp_0(ge_precomp h) {
        fe_1.fe_1(h.yplusx);
        fe_1.fe_1(h.yminusx);
        fe_0.fe_0(h.xy2d);
    }


}
