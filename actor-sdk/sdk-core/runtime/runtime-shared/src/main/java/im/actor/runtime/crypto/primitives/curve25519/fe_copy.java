package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class fe_copy {

//CONVERT #include "fe.h"

    /*
        h = f
    */
    public static void fe_copy(int[] h, int[] f) {
        int f0 = f[0];
        int f1 = f[1];
        int f2 = f[2];
        int f3 = f[3];
        int f4 = f[4];
        int f5 = f[5];
        int f6 = f[6];
        int f7 = f[7];
        int f8 = f[8];
        int f9 = f[9];
        h[0] = f0;
        h[1] = f1;
        h[2] = f2;
        h[3] = f3;
        h[4] = f4;
        h[5] = f5;
        h[6] = f6;
        h[7] = f7;
        h[8] = f8;
        h[9] = f9;
    }


}
