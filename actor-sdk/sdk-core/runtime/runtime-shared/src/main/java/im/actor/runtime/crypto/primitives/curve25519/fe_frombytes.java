package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class fe_frombytes {

//CONVERT #include "fe.h"
//CONVERT #include "long.h"
//CONVERT #include "long.h"

    public static long load_3(byte[] in, int index) {
        long result;
        result = ((long) in[index + 0]) & 0xFF;
        result |= (((long) in[index + 1]) << 8) & 0xFF00;
        result |= (((long) in[index + 2]) << 16) & 0xFF0000;
        return result;
    }

    public static long load_4(byte[] in, int index) {
        long result;
        result = (((long) in[index + 0]) & 0xFF);
        result |= ((((long) in[index + 1]) << 8) & 0xFF00);
        result |= ((((long) in[index + 2]) << 16) & 0xFF0000);
        result |= ((((long) in[index + 3]) << 24) & 0xFF000000L);
        return result;
    }

/*
Ignores top bit of h.
*/

    public static void fe_frombytes(int[] h, byte[] s) {
        long h0 = load_4(s, 0);
        long h1 = load_3(s, 4) << 6;
        long h2 = load_3(s, 7) << 5;
        long h3 = load_3(s, 10) << 3;
        long h4 = load_3(s, 13) << 2;
        long h5 = load_4(s, 16);
        long h6 = load_3(s, 20) << 7;
        long h7 = load_3(s, 23) << 5;
        long h8 = load_3(s, 26) << 4;
        long h9 = (load_3(s, 29) & 8388607) << 2;
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
