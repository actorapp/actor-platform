package im.actor.runtime.crypto.primitives.streebog;

import im.actor.runtime.crypto.primitives.util.Pack;

public class StreebogFastDigest {

    private final int hashLength;
    private byte[] h = new byte[64];
    private byte[] m = new byte[64];
    private byte[] e = new byte[64];

    private long[] tmpT = new long[8];
    private long[] tmpS = new long[8];
    private long[] tmpK = new long[8];

    private int pt;
    private long n;

    public StreebogFastDigest(int hashLength) {
        this.hashLength = hashLength;
        reset();
    }

    public void reset() {
        for (int i = 0; i < 64; i++) {
            if (hashLength == 32) {
                h[i] = (byte) 0x01;
            } else {
                h[i] = (byte) 0x00;
            }
            e[i] = (byte) 0x00;
        }

        pt = 63;
        n = 0;
    }

    public void update(byte[] in, int offset, int length) {

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

        m[pt] = 1;
        for (int i = 0; i < pt; i++) {
            m[i] = 0;
        }

        streebog_g(h, m, n);

        int c = 0;
        for (int i = 63; i >= 0; i--) {
            c += (e[i] & 0xFF) + (m[i] & 0xFF);
            e[i] = (byte) (c & 0xFF);
            c >>= 8;
        }


        for (int j = 0; j < 64; j++) {
            m[j] = (byte) 0x00;
        }

        // sbx->n += (63 - sbx->pt) << 3;      // total bits
        n += (63 - pt) << 3;
        for (int i = 63; n > 0; i--) {
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

    private void streebog_g(byte[] h, byte[] m, long n) {

        long[] hl = new long[8];
        long[] ml = new long[8];
        Pack.bigEndianToLong(h, 0, hl);
        Pack.bigEndianToLong(m, 0, ml);

        tmpT[0] = hl[0];
        tmpT[1] = hl[1];
        tmpT[2] = hl[2];
        tmpT[3] = hl[3];
        tmpT[4] = hl[4];
        tmpT[5] = hl[5];
        tmpT[6] = hl[6];
        tmpT[7] = hl[7];

        byte[] tt = Pack.longToBigEndian(tmpT);
        for (int i = 63; n > 0; i--) {
            // t.b[i] ^= n & 0xFF;
            tt[i] = (byte) (tt[i] ^ ((byte) (n & 0xFF)));
            // n >>= 8;
            n >>= 8;
        }
        Pack.bigEndianToLong(tt, 0, tmpT);

        tmpK[0] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 56) & 0xFF)];
        tmpK[1] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 48) & 0xFF)];
        tmpK[2] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 40) & 0xFF)];
        tmpK[3] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 32) & 0xFF)];
        tmpK[4] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 24) & 0xFF)];
        tmpK[5] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 16) & 0xFF)];
        tmpK[6] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 8) & 0xFF)];
        tmpK[7] = StreebogTables.sbob_sl64[0][(int) (tmpT[0] & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) (tmpT[1] & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) (tmpT[2] & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) (tmpT[3] & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) (tmpT[4] & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) (tmpT[5] & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) (tmpT[6] & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) (tmpT[7] & 0xFF)];

        tmpS[0] = ml[0];
        tmpS[1] = ml[1];
        tmpS[2] = ml[2];
        tmpS[3] = ml[3];
        tmpS[4] = ml[4];
        tmpS[5] = ml[5];
        tmpS[6] = ml[6];
        tmpS[7] = ml[7];

        for (int r = 0; r < 12; r++) {

            tmpT[0] = tmpS[0] ^ tmpK[0];
            tmpT[1] = tmpS[1] ^ tmpK[1];
            tmpT[2] = tmpS[2] ^ tmpK[2];
            tmpT[3] = tmpS[3] ^ tmpK[3];
            tmpT[4] = tmpS[4] ^ tmpK[4];
            tmpT[5] = tmpS[5] ^ tmpK[5];
            tmpT[6] = tmpS[6] ^ tmpK[6];
            tmpT[7] = tmpS[7] ^ tmpK[7];

            tmpS[0] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 56) & 0xFF)];
            tmpS[1] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 48) & 0xFF)];
            tmpS[2] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 40) & 0xFF)];
            tmpS[3] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 32) & 0xFF)];
            tmpS[4] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 24) & 0xFF)];
            tmpS[5] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 16) & 0xFF)];
            tmpS[6] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 8) & 0xFF)];
            tmpS[7] = StreebogTables.sbob_sl64[0][(int) (tmpT[0] & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) (tmpT[1] & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) (tmpT[2] & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) (tmpT[3] & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) (tmpT[4] & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) (tmpT[5] & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) (tmpT[6] & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) (tmpT[7] & 0xFF)];

            tmpT[0] = tmpK[0] ^ StreebogTables.sbob_rc64[r][0];
            tmpT[1] = tmpK[1] ^ StreebogTables.sbob_rc64[r][1];
            tmpT[2] = tmpK[2] ^ StreebogTables.sbob_rc64[r][2];
            tmpT[3] = tmpK[3] ^ StreebogTables.sbob_rc64[r][3];
            tmpT[4] = tmpK[4] ^ StreebogTables.sbob_rc64[r][4];
            tmpT[5] = tmpK[5] ^ StreebogTables.sbob_rc64[r][5];
            tmpT[6] = tmpK[6] ^ StreebogTables.sbob_rc64[r][6];
            tmpT[7] = tmpK[7] ^ StreebogTables.sbob_rc64[r][7];


            tmpK[0] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 56) & 0xFF)];
            tmpK[1] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 48) & 0xFF)];
            tmpK[2] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 40) & 0xFF)];
            tmpK[3] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 32) & 0xFF)];
            tmpK[4] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 24) & 0xFF)];
            tmpK[5] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 16) & 0xFF)];
            tmpK[6] = StreebogTables.sbob_sl64[0][(int) ((tmpT[0] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT[1] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT[2] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT[3] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT[4] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT[5] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT[6] >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT[7] >> 8) & 0xFF)];
            tmpK[7] = StreebogTables.sbob_sl64[0][(int) (tmpT[0] & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) (tmpT[1] & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) (tmpT[2] & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) (tmpT[3] & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) (tmpT[4] & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) (tmpT[5] & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) (tmpT[6] & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) (tmpT[7] & 0xFF)];
        }

        hl[0] = hl[0] ^ tmpS[0] ^ tmpK[0] ^ ml[0];
        hl[1] = hl[1] ^ tmpS[1] ^ tmpK[1] ^ ml[1];
        hl[2] = hl[2] ^ tmpS[2] ^ tmpK[2] ^ ml[2];
        hl[3] = hl[3] ^ tmpS[3] ^ tmpK[3] ^ ml[3];
        hl[4] = hl[4] ^ tmpS[4] ^ tmpK[4] ^ ml[4];
        hl[5] = hl[5] ^ tmpS[5] ^ tmpK[5] ^ ml[5];
        hl[6] = hl[6] ^ tmpS[6] ^ tmpK[6] ^ ml[6];
        hl[7] = hl[7] ^ tmpS[7] ^ tmpK[7] ^ ml[7];

        Pack.longToBigEndian(hl, h, 0);
    }
}
