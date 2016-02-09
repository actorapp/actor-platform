package im.actor.crypto.primitives.streebog;

import im.actor.crypto.primitives.util.ByteStrings;

public class StreebogDigest {

    private final int hashLength;
    private byte[] h = new byte[64];
    private byte[] m = new byte[64];
    private byte[] e = new byte[64];

    private byte[] tmpT = new byte[64];
    private byte[] tmpS = new byte[64];
    private byte[] tmpK = new byte[64];

    private int pt;
    private long n;

    public StreebogDigest(int hashLength) {
        this.hashLength = hashLength;
        reset();
    }

    public void reset() {
        // IV:: 01010.. for 256-bit hash, 0000... for 512-bit hash
        // memset(&sbx->h, hlen == 32 ? 0x01 : 0x00, 64);
        // memset(&sbx->e, 0x00, 64);
        for (int i = 0; i < 64; i++) {
            if (hashLength == 32) {
                h[i] = (byte) 0x01;
            } else {
                h[i] = (byte) 0x00;
            }
            e[i] = (byte) 0x00;
        }

        // sbx->pt = 63;
        // sbx->n = 0;
        pt = 63;
        n = 0;
    }

    public void update(byte[] in, int offset, int length) {

        // j = sbx->pt;
        int j = pt;
        for (int i = 0; i < length; i++) {
            m[j--] = in[offset + i];

            // compress
            // if (j < 0) {
            if (j < 0) {
                // streebog_g(&sbx->h, &sbx->m, sbx->n);
                streebog_g(h, m, n);
                // sbx->n += 0x200;
                n += 0x200;

                // epsilon summation
                // c = 0;
                int c = 0;

                // for (j = 63; j >= 0; j--) {
                for (j = 63; j >= 0; j--) {
                    // c += sbx->e.b[j] + sbx->m.b[j];
                    c += (e[j] & 0xFF) + (m[j] & 0xFF);
                    // sbx->e.b[j] = c & 0xFF;
                    e[j] = (byte) (c & 0xFF);
                    // c >>= 8;
                    c >>= 8;
                }

                // j = 63;
                j = 63;
            }
        }

        // sbx->pt = j;
        pt = j;

    }

    public void doFinal(byte[] out, int offset) {
        // pad the message and run final g
        // i = sbx->pt;
        int i = pt;
        // sbx->m.b[i--] = 0x01;
        m[i--] = 1;
        while (i >= 0) {
            // sbx->m.b[i--] = 0x00;
            m[i--] = 0;
        }
        // streebog_g(&sbx->h, &sbx->m, sbx->n);
        streebog_g(h, m, n);

        // epsilon summation
        int c = 0;
        for (i = 63; i >= 0; i--) {
            // c += sbx->e.b[i] + sbx->m.b[i];
            c += (e[i] & 0xFF) + (m[i] & 0xFF);
            // sbx->e.b[i] = c & 0xFF;
            e[i] = (byte) (c & 0xFF);
            // c >>= 8;
            c >>= 8;
        }

        // finalization n
        // memset(&sbx->m, 0x00, 64);
        for (int j = 0; j < 64; j++) {
            m[j] = (byte) 0x00;
        }

        // sbx->n += (63 - sbx->pt) << 3;      // total bits
        n += (63 - pt) << 3;
        for (i = 63; n > 0; i--) {
            // sbx->m.b[i] = sbx->n & 0xFF;
            m[i] = (byte) (n & 0xFF);
            // sbx->n >>= 8;
            n >>= 8;
        }

        // streebog_g(&sbx->h, &sbx->m, 0);
        // streebog_g(&sbx->h, &sbx->e, 0);
        streebog_g(h, m, 0);
        streebog_g(h, e, 0);

        // copy the result
        // memcpy(hash, &sbx->h, sbx->hlen);
        for (int j = 0; j < hashLength; j++) {
            out[offset + j] = h[j];
        }

        // clear out sensitive stuff
        reset();
    }

    //    #define SBOG_LPSti64 \
//            (sbob_sl64[0][t.b[i]] ^     sbob_sl64[1][t.b[i + 8]] ^  \
//    sbob_sl64[2][t.b[i + 16]] ^ sbob_sl64[3][t.b[i + 24]] ^ \
//    sbob_sl64[4][t.b[i + 32]] ^ sbob_sl64[5][t.b[i + 40]] ^ \
//    sbob_sl64[6][t.b[i + 48]] ^ sbob_sl64[7][t.b[i + 56]])

    private static long SBOG_LPSti64(byte[] t, int i) {
        return (StreebogTables.sbob_sl64[0][t[i] & 0xFF] ^
                StreebogTables.sbob_sl64[1][t[i + 8] & 0xFF] ^
                StreebogTables.sbob_sl64[2][t[i + 16] & 0xFF] ^
                StreebogTables.sbob_sl64[3][t[i + 24] & 0xFF] ^
                StreebogTables.sbob_sl64[4][t[i + 32] & 0xFF] ^
                StreebogTables.sbob_sl64[5][t[i + 40] & 0xFF] ^
                StreebogTables.sbob_sl64[6][t[i + 48] & 0xFF] ^
                StreebogTables.sbob_sl64[7][t[i + 56] & 0xFF]);
    }

    private void streebog_g(byte[] h, byte[] m, long n) {
        // w512_t k, s, t;
        // k = LPS(h ^ n)
        // memcpy(&t, h, 64);

        copy(h, tmpT);
        // zero(tmpK);
        // zero(tmpS);

        // for (i = 63; n > 0; i--) {
        for (int i = 63; n > 0; i--) {
            // t.b[i] ^= n & 0xFF;
            tmpT[i] = (byte) (tmpT[i] ^ ((byte) (n & 0xFF)));
            // n >>= 8;
            n >>= 8;
        }

        for (int i = 0; i < 8; i++) {
            // k.q[i] = SBOG_LPSti64;
            setWord64(tmpK, i, SBOG_LPSti64(tmpT, i));
        }

        // s = m
        // memcpy(&s, m, 64);
        copy(m, tmpS);
        // s.setBytes(m.getBytes());

        for (int r = 0; r < 12; r++) {
            // s = LPS(s ^ k)
            for (int i = 0; i < 8; i++) {
                // t.q[i] = s.q[i] ^ k.q[i];
                setWord64(tmpT, i, getWord64(tmpS, i) ^ getWord64(tmpK, i));
            }
            for (int i = 0; i < 8; i++) {
                // s.q[i] = SBOG_LPSti64;
                setWord64(tmpS, i, SBOG_LPSti64(tmpT, i));
            }

            // k = LPS(k ^ c[i])
            for (int i = 0; i < 8; i++) {
                // t.q[i] = k.q[i] ^ sbob_rc64[r][i];
                setWord64(tmpT, i, getWord64(tmpK, i) ^ StreebogTables.sbob_rc64[r][i]);
            }

            for (int i = 0; i < 8; i++) {
                // k.q[i] = SBOG_LPSti64;
                setWord64(tmpK, i, SBOG_LPSti64(tmpT, i));
            }
        }

        for (int i = 0; i < 64; i++) {
            // h->q[i] ^= s.q[i] ^ k.q[i] ^ m->q[i];
            h[i] = (byte) ((h[i] ^ tmpS[i] ^ tmpK[i] ^ m[i]) & 0xFF);
        }
    }

    private void copy(byte[] from, byte[] dest) {
        System.arraycopy(from, 0, dest, 0, 64);
    }

    private long getWord64(byte[] value, int index) {
        return ByteStrings.bytesToLong(value, index * 8);
    }

    private void setWord64(byte[] dest, int index, long val) {
        ByteStrings.write(dest, index * 8, ByteStrings.longToBytes(val), 0, 8);
    }
}
