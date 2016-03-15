package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class fe_mul121666 {

//CONVERT #include "fe.h"
//CONVERT #include "long.h"

/*
h = f * 121666
Can overlap h with f.

Preconditions:
   |f| bounded by 1.1*2^26,1.1*2^25,1.1*2^26,1.1*2^25,etc.

Postconditions:
   |h| bounded by 1.1*2^25,1.1*2^24,1.1*2^25,1.1*2^24,etc.
*/

    public static void fe_mul121666(int[] h, int[] f) {
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
        long h0 = f0 * (long) 121666;
        long h1 = f1 * (long) 121666;
        long h2 = f2 * (long) 121666;
        long h3 = f3 * (long) 121666;
        long h4 = f4 * (long) 121666;
        long h5 = f5 * (long) 121666;
        long h6 = f6 * (long) 121666;
        long h7 = f7 * (long) 121666;
        long h8 = f8 * (long) 121666;
        long h9 = f9 * (long) 121666;
        long carry0;
        long carry1;
        long carry2;
        long carry3;
        long carry4;
        long carry5;
        long carry6;
        long carry7;
        long carry8;
        long carry9;

        carry9 = (h9 + (long) (1 << 24)) >> 25;
        h0 += carry9 * 19;
        h9 -= carry9 << 25;
        carry1 = (h1 + (long) (1 << 24)) >> 25;
        h2 += carry1;
        h1 -= carry1 << 25;
        carry3 = (h3 + (long) (1 << 24)) >> 25;
        h4 += carry3;
        h3 -= carry3 << 25;
        carry5 = (h5 + (long) (1 << 24)) >> 25;
        h6 += carry5;
        h5 -= carry5 << 25;
        carry7 = (h7 + (long) (1 << 24)) >> 25;
        h8 += carry7;
        h7 -= carry7 << 25;

        carry0 = (h0 + (long) (1 << 25)) >> 26;
        h1 += carry0;
        h0 -= carry0 << 26;
        carry2 = (h2 + (long) (1 << 25)) >> 26;
        h3 += carry2;
        h2 -= carry2 << 26;
        carry4 = (h4 + (long) (1 << 25)) >> 26;
        h5 += carry4;
        h4 -= carry4 << 26;
        carry6 = (h6 + (long) (1 << 25)) >> 26;
        h7 += carry6;
        h6 -= carry6 << 26;
        carry8 = (h8 + (long) (1 << 25)) >> 26;
        h9 += carry8;
        h8 -= carry8 << 26;

        h[0] = (int) h0;
        h[1] = (int) h1;
        h[2] = (int) h2;
        h[3] = (int) h3;
        h[4] = (int) h4;
        h[5] = (int) h5;
        h[6] = (int) h6;
        h[7] = (int) h7;
        h[8] = (int) h8;
        h[9] = (int) h9;
    }


}
