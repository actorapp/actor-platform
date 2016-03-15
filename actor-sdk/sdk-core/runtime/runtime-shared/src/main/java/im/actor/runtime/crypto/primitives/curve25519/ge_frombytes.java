package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ge_frombytes {

//CONVERT #include "ge.h"

    static int[] d = {
//CONVERT #include "d.h"
            -10913610, 13857413, -15372611, 6949391, 114729, -8787816, -6275908, -3247719, -18696448, -12055116
    };

    static int[] sqrtm1 = {
//CONVERT #include "sqrtm1.h"
            -32595792, -7943725, 9377950, 3500415, 12389472, -272473, -25146209, -2005654, 326686, 11406482
    };

    static int ge_frombytes_negate_vartime(ge_p3 h, byte[] s) {
        int[] u = new int[10];
        int[] v = new int[10];
        int[] v3 = new int[10];
        int[] vxx = new int[10];
        int[] check = new int[10];

        fe_frombytes.fe_frombytes(h.Y, s);
        fe_1.fe_1(h.Z);
        fe_sq.fe_sq(u, h.Y);
        fe_mul.fe_mul(v, u, d);
        fe_sub.fe_sub(u, u, h.Z);       /* u = y^2-1 */
        fe_add.fe_add(v, v, h.Z);       /* v = dy^2+1 */

        fe_sq.fe_sq(v3, v);
        fe_mul.fe_mul(v3, v3, v);        /* v3 = v^3 */
        fe_sq.fe_sq(h.X, v3);
        fe_mul.fe_mul(h.X, h.X, v);
        fe_mul.fe_mul(h.X, h.X, u);    /* x = uv^7 */

        fe_pow22523.fe_pow22523(h.X, h.X); /* x = (uv^7)^((q-5)/8) */
        fe_mul.fe_mul(h.X, h.X, v3);
        fe_mul.fe_mul(h.X, h.X, u);    /* x = uv^3(uv^7)^((q-5)/8) */

        fe_sq.fe_sq(vxx, h.X);
        fe_mul.fe_mul(vxx, vxx, v);
        fe_sub.fe_sub(check, vxx, u);    /* vx^2-u */
        if (fe_isnonzero.fe_isnonzero(check) != 0) {
            fe_add.fe_add(check, vxx, u);  /* vx^2+u */
            if (fe_isnonzero.fe_isnonzero(check) != 0) return -1;
            fe_mul.fe_mul(h.X, h.X, sqrtm1);
        }

        if (fe_isnegative.fe_isnegative(h.X) == ((s[31] >>> 7) & 0x01)) {
            fe_neg.fe_neg(h.X, h.X);
        }

        fe_mul.fe_mul(h.T, h.X, h.Y);
        return 0;
    }


}
