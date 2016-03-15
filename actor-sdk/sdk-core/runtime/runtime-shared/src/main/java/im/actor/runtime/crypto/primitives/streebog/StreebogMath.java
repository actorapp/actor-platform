package im.actor.runtime.crypto.primitives.streebog;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ALL_CHECKS 1
]-*/

/**
 * Java-port of Streebog implementation by Markku-Juhani O. Saarinen <mjos@iki.fi>
 * https://github.com/mjosaarinen/stricat/blob/master/streebog.c
 * <p>
 * Ported by Steve Kite (steve@actor.im)
 */
class StreebogMath {

//    #define SBOG_LPSti64 \
//            (sbob_sl64[0][t.b[i]] ^     sbob_sl64[1][t.b[i + 8]] ^  \
//    sbob_sl64[2][t.b[i + 16]] ^ sbob_sl64[3][t.b[i + 24]] ^ \
//    sbob_sl64[4][t.b[i + 32]] ^ sbob_sl64[5][t.b[i + 40]] ^ \
//    sbob_sl64[6][t.b[i + 48]] ^ sbob_sl64[7][t.b[i + 56]])

    private static long SBOG_LPSti64(Int512 t, int i) {
        return (StreebogTables.sbob_sl64[0][t.getByte(i) & 0xFF] ^
                StreebogTables.sbob_sl64[1][t.getByte(i + 8) & 0xFF] ^
                StreebogTables.sbob_sl64[2][t.getByte(i + 16) & 0xFF] ^
                StreebogTables.sbob_sl64[3][t.getByte(i + 24) & 0xFF] ^
                StreebogTables.sbob_sl64[4][t.getByte(i + 32) & 0xFF] ^
                StreebogTables.sbob_sl64[5][t.getByte(i + 40) & 0xFF] ^
                StreebogTables.sbob_sl64[6][t.getByte(i + 48) & 0xFF] ^
                StreebogTables.sbob_sl64[7][t.getByte(i + 56) & 0xFF]);
    }

    public static void streebog_g(Int512 h, Int512 m, long n) {
        // w512_t k, s, t;
        // k = LPS(h ^ n)
        // memcpy(&t, h, 64);
        Int512 t = new Int512(h.getBytes());
        Int512 k = new Int512();
        Int512 s = new Int512();

        // for (i = 63; n > 0; i--) {
        for (int i = 63; n > 0; i--) {
            // t.b[i] ^= n & 0xFF;
            t.setByte(i, (byte) (t.getByte(i) ^ ((byte) (n & 0xFF))));
            // n >>= 8;
            n = (n >> 8) & 0xFFFFFFFFFFFFFFFFL;
        }

        for (int i = 0; i < 8; i++) {
            // k.q[i] = SBOG_LPSti64;
            k.setWord64(i, SBOG_LPSti64(t, i));
        }

        // s = m
        // memcpy(&s, m, 64);
        s.setBytes(m.getBytes());

        for (int r = 0; r < 12; r++) {
            // s = LPS(s ^ k)
            for (int i = 0; i < 8; i++) {
                // t.q[i] = s.q[i] ^ k.q[i];
                t.setWord64(i, s.getWord64(i) ^ k.getWord64(i));
            }
            for (int i = 0; i < 8; i++) {
                // s.q[i] = SBOG_LPSti64;
                s.setWord64(i, SBOG_LPSti64(t, i));
            }

            // k = LPS(k ^ c[i])
            for (int i = 0; i < 8; i++) {
                // t.q[i] = k.q[i] ^ sbob_rc64[r][i];
                t.setWord64(i, k.getWord64(i) ^ StreebogTables.sbob_rc64[r][i]);
            }

            for (int i = 0; i < 8; i++) {
                // k.q[i] = SBOG_LPSti64;
                k.setWord64(i, SBOG_LPSti64(t, i));
            }
        }

        for (int i = 0; i < 8; i++) {
            // h->q[i] ^= s.q[i] ^ k.q[i] ^ m->q[i];
            h.setWord64(i, h.getWord64(i) ^ s.getWord64(i) ^ k.getWord64(i) ^ m.getWord64(i));
        }
    }
}
