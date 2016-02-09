package im.actor.crypto.primitives.kuznechik;

import im.actor.crypto.primitives.util.Pack;

/**
 * Mathematical methods for Kuzhechik encryption
 * <p/>
 * Ported by Steven Kite (steve@actor.im) from
 * https://github.com/mjosaarinen/kuznechik/blob/master/kuznechik_8bit.c
 * Multiplication optimization from
 * http://www.cs.utsa.edu/~wagner/laws/FFM.html
 */
public class KuznechikMath {

    // poly multiplication mod p(x) = x^8 + x^7 + x^6 + x + 1
    // totally not constant time
    public static byte kuz_mul_gf256(byte x, byte y) {
        // uint8_t z;
        // z = 0;
        byte z = 0;

        // while (y) {
        while ((y & 0xFF) != 0) {
            // if (y & 1)
            if ((y & 1) != 0) {
                // z ^= x;
                z ^= x;
            }

            // x = (x << 1) ^ (x & 0x80 ? 0xC3 : 0x00);
            x = (byte) (((x & 0xFF) << 1) ^ ((x & 0x80) != 0 ? 0xC3 : 0x00));
            // y >>= 1;
            y = (byte) ((y & 0xFF) >> 1);
        }

        // return z;
        return z;
    }

    // Fast implementation of multiplication in GF(2^8) on x^8 + x^7 + x^6 + x + 1
    // Implemented with
    public static byte kuz_mul_gf256_fast(byte a, byte b) {
        if (a == 0 || b == 0) return 0;
        int t = (KuznechikTables.gf256_L[(a & 0xff)] & 0xff) + (KuznechikTables.gf256_L[(b & 0xff)] & 0xff);
        if (t > 255) t = t - 255;
        return KuznechikTables.gf256_E[(t & 0xff)];
    }

    // linear operation l
    // static void kuz_l(w128_t *w)
    public static void kuz_l(Kuz128 w) {
        kuz_l(w.getB());
    }

    public static void kuz_l(int[] w, byte[] tmp) {
        Pack.intToBigEndian(w, tmp, 0);
        kuz_l(tmp);
        Pack.bigEndianToInt(tmp, 0, w);
    }

    public static void kuz_l(byte[] w) {
        for (int j = 0; j < 16; j++) {
            byte x = w[15];
            w[15] = w[14];
            x ^= kuz_mul_gf256_fast(w[14], KuznechikTables.kuz_lvec[14]);
            w[14] = w[13];
            x ^= kuz_mul_gf256_fast(w[13], KuznechikTables.kuz_lvec[13]);
            w[13] = w[12];
            x ^= kuz_mul_gf256_fast(w[12], KuznechikTables.kuz_lvec[12]);
            w[12] = w[11];
            x ^= kuz_mul_gf256_fast(w[11], KuznechikTables.kuz_lvec[11]);
            w[11] = w[10];
            x ^= kuz_mul_gf256_fast(w[10], KuznechikTables.kuz_lvec[10]);
            w[10] = w[9];
            x ^= kuz_mul_gf256_fast(w[9], KuznechikTables.kuz_lvec[9]);
            w[9] = w[8];
            x ^= kuz_mul_gf256_fast(w[8], KuznechikTables.kuz_lvec[8]);
            w[8] = w[7];
            x ^= kuz_mul_gf256_fast(w[7], KuznechikTables.kuz_lvec[7]);
            w[7] = w[6];
            x ^= kuz_mul_gf256_fast(w[6], KuznechikTables.kuz_lvec[6]);
            w[6] = w[5];
            x ^= kuz_mul_gf256_fast(w[5], KuznechikTables.kuz_lvec[5]);
            w[5] = w[4];
            x ^= kuz_mul_gf256_fast(w[4], KuznechikTables.kuz_lvec[4]);
            w[4] = w[3];
            x ^= kuz_mul_gf256_fast(w[3], KuznechikTables.kuz_lvec[3]);
            w[3] = w[2];
            x ^= kuz_mul_gf256_fast(w[2], KuznechikTables.kuz_lvec[2]);
            w[2] = w[1];
            x ^= kuz_mul_gf256_fast(w[1], KuznechikTables.kuz_lvec[1]);
            w[1] = w[0];
            x ^= kuz_mul_gf256_fast(w[0], KuznechikTables.kuz_lvec[0]);
            w[0] = x;
        }
    }

    // inverse of linear operation l
    public static void kuz_l_inv(Kuz128 w) {

        // 16 rounds
        for (int j = 0; j < 16; j++) {

            // x = w->b[0];
            byte x = w.getB()[0];

            for (int i = 0; i < 15; i++) {

                // w->b[i] = w->b[i + 1];
                w.getB()[i] = w.getB()[i + 1];

                // x ^= kuz_mul_gf256(w->b[i], kuz_lvec[i]);
                x ^= kuz_mul_gf256_fast(w.getB()[i], KuznechikTables.kuz_lvec[i]);
            }

            // w->b[15] = x;
            w.getB()[15] = x;
        }
    }
}
