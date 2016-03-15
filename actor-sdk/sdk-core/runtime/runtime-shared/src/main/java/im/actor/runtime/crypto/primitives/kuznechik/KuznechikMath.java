package im.actor.runtime.crypto.primitives.kuznechik;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ALL_CHECKS 1
]-*/

/**
 * Mathematical methods for Kuzhechik encryption
 * <p>
 * Ported by Steven Kite (steve@actor.im) from
 * https://github.com/mjosaarinen/kuznechik/blob/master/kuznechik_8bit.c
 * Multiplication optimization from
 * http://www.cs.utsa.edu/~wagner/laws/FFM.html
 */
class KuznechikMath {

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
        // 16 rounds
        for (int j = 0; j < 16; j++) {
            // An LFSR with 16 elements from GF(2^8)
            // x = w->b[15];	// since lvec[15] = 1
            byte x = w.getB()[15];

            for (int i = 14; i >= 0; i--) {
                // w->b[i + 1] = w->b[i];
                w.getB()[i + 1] = w.getB()[i];
                // x ^= kuz_mul_gf256(w->b[i], kuz_lvec[i]);
                x ^= kuz_mul_gf256_fast(w.getB()[i], KuznechikTables.kuz_lvec[i]);
            }
            w.getB()[0] = x;
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
