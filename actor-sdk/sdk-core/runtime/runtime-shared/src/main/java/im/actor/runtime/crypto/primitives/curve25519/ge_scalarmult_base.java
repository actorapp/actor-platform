package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ge_scalarmult_base {

//CONVERT #include "ge.h"
//CONVERT #include "crypto_uint32.h"

    static int equal(byte b, byte c) {
        int ub = b;
        int uc = c;
        int x = ub ^ uc; /* 0: yes; 1..255: no */
        int y = x; /* 0: yes; 1..255: no */
        y -= 1; /* 4294967295: yes; 0..254: no */
        y >>>= 31; /* 1: yes; 0: no */
        return y;
    }

    static int negative(byte b) {
        long x = b; /* 18446744073709551361..18446744073709551615: yes; 0..255: no */
        x >>>= 63; /* 1: yes; 0: no */
        return (int) x;
    }

    static void cmov(ge_precomp t, ge_precomp u, int b) {
        fe_cmov.fe_cmov(t.yplusx, u.yplusx, b);
        fe_cmov.fe_cmov(t.yminusx, u.yminusx, b);
        fe_cmov.fe_cmov(t.xy2d, u.xy2d, b);
    }

    static void select(ge_precomp t, int pos, byte b) {
        ge_precomp base[][] = (pos <= 7 ? ge_precomp_base_0_7.base :
                (pos <= 15 ? ge_precomp_base_8_15.base :
                        (pos <= 23 ? ge_precomp_base_16_23.base : ge_precomp_base_24_31.base)));

        ge_precomp minust = new ge_precomp();
        int bnegative = negative(b);
        int babs = b - (((-bnegative) & b) << 1);

        ge_precomp_0.ge_precomp_0(t);
        cmov(t, base[pos][0], equal((byte) babs, (byte) 1));
        cmov(t, base[pos][1], equal((byte) babs, (byte) 2));
        cmov(t, base[pos][2], equal((byte) babs, (byte) 3));
        cmov(t, base[pos][3], equal((byte) babs, (byte) 4));
        cmov(t, base[pos][4], equal((byte) babs, (byte) 5));
        cmov(t, base[pos][5], equal((byte) babs, (byte) 6));
        cmov(t, base[pos][6], equal((byte) babs, (byte) 7));
        cmov(t, base[pos][7], equal((byte) babs, (byte) 8));
        fe_copy.fe_copy(minust.yplusx, t.yminusx);
        fe_copy.fe_copy(minust.yminusx, t.yplusx);
        fe_neg.fe_neg(minust.xy2d, t.xy2d);
        cmov(t, minust, bnegative);
    }

/*
h = a * B
where a = a[0]+256*a[1]+...+256^31 a[31]
B is the Ed25519 base point (x,4/5) with x positive.

Preconditions:
  a[31] <= 127
*/

    public static void ge_scalarmult_base(ge_p3 h, byte[] a) {
        byte[] e = new byte[64];
        byte carry;
        ge_p1p1 r = new ge_p1p1();
        ge_p2 s = new ge_p2();
        ge_precomp t = new ge_precomp();
        int i;

        for (i = 0; i < 32; ++i) {
            e[2 * i + 0] = (byte) ((a[i] >>> 0) & 15);
            e[2 * i + 1] = (byte) ((a[i] >>> 4) & 15);
        }
  /* each e[i] is between 0 and 15 */
  /* e[63] is between 0 and 7 */

        carry = 0;
        for (i = 0; i < 63; ++i) {
            e[i] += carry;
            carry = (byte) (e[i] + 8);
            carry >>= 4;
            e[i] -= carry << 4;
        }
        e[63] += carry;
  /* each e[i] is between -8 and 8 */

        ge_p3_0.ge_p3_0(h);
        for (i = 1; i < 64; i += 2) {
            select(t, i / 2, e[i]);
            ge_madd.ge_madd(r, h, t);
            ge_p1p1_to_p3.ge_p1p1_to_p3(h, r);
        }

        ge_p3_dbl.ge_p3_dbl(r, h);
        ge_p1p1_to_p2.ge_p1p1_to_p2(s, r);
        ge_p2_dbl.ge_p2_dbl(r, s);
        ge_p1p1_to_p2.ge_p1p1_to_p2(s, r);
        ge_p2_dbl.ge_p2_dbl(r, s);
        ge_p1p1_to_p2.ge_p1p1_to_p2(s, r);
        ge_p2_dbl.ge_p2_dbl(r, s);
        ge_p1p1_to_p3.ge_p1p1_to_p3(h, r);

        for (i = 0; i < 64; i += 2) {
            select(t, i / 2, e[i]);
            ge_madd.ge_madd(r, h, t);
            ge_p1p1_to_p3.ge_p1p1_to_p3(h, r);
        }
    }

}
