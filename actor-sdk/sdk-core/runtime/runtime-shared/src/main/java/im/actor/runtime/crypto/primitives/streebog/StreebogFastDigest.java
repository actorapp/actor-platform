package im.actor.runtime.crypto.primitives.streebog;

import im.actor.runtime.crypto.primitives.util.Pack;

public class StreebogFastDigest {

    private final int hashLength;
    private long[] h = new long[8];
    private byte[] m = new byte[64];
    private byte[] e = new byte[64];

    private int pt;
    private long n;

    public StreebogFastDigest(int hashLength) {
        this.hashLength = hashLength;
        reset();
    }

    public void reset() {

        if (hashLength == 32) {
            h[0] = 0x0101010101010101L;
            h[1] = 0x0101010101010101L;
            h[2] = 0x0101010101010101L;
            h[3] = 0x0101010101010101L;
            h[4] = 0x0101010101010101L;
            h[5] = 0x0101010101010101L;
            h[6] = 0x0101010101010101L;
            h[7] = 0x0101010101010101L;
        } else {
            h[0] = 0L;
            h[1] = 0L;
            h[2] = 0L;
            h[3] = 0L;
            h[4] = 0L;
            h[5] = 0L;
            h[6] = 0L;
            h[7] = 0L;
        }

        for (int i = 0; i < 64; i++) {
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
                streebog_g(m, n);
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

        streebog_g(m, n);

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
        streebog_g(m, 0);
        streebog_g(e, 0);

        // copy the result

        Pack.longToBigEndian(h, out, offset, hashLength / 8);

        // clear out sensitive stuff
        reset();
    }

    private void streebog_g(byte[] m, long n) {

        // long[] hl = new long[8];
        long[] ml = new long[8];
        // Pack.bigEndianToLong(h, 0, hl);
        Pack.bigEndianToLong(m, 0, ml);

        long tmpT0 = h[0];
        long tmpT1 = h[1];
        long tmpT2 = h[2];
        long tmpT3 = h[3];
        long tmpT4 = h[4];
        long tmpT5 = h[5];
        long tmpT6 = h[6];
        long tmpT7 = h[7] ^ n;

        long tmpK0 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 56) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 56) & 0xFF)];
        long tmpK1 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 48) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 48) & 0xFF)];
        long tmpK2 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 40) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 40) & 0xFF)];
        long tmpK3 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 32) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 32) & 0xFF)];
        long tmpK4 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 24) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 24) & 0xFF)];
        long tmpK5 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 16) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 16) & 0xFF)];
        long tmpK6 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 8) & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 8) & 0xFF)];
        long tmpK7 = StreebogTables.sbob_sl64[0][(int) (tmpT0 & 0xFF)] ^
                StreebogTables.sbob_sl64[1][(int) (tmpT1 & 0xFF)] ^
                StreebogTables.sbob_sl64[2][(int) (tmpT2 & 0xFF)] ^
                StreebogTables.sbob_sl64[3][(int) (tmpT3 & 0xFF)] ^
                StreebogTables.sbob_sl64[4][(int) (tmpT4 & 0xFF)] ^
                StreebogTables.sbob_sl64[5][(int) (tmpT5 & 0xFF)] ^
                StreebogTables.sbob_sl64[6][(int) (tmpT6 & 0xFF)] ^
                StreebogTables.sbob_sl64[7][(int) (tmpT7 & 0xFF)];


        long tmpS0 = ml[0];
        long tmpS1 = ml[1];
        long tmpS2 = ml[2];
        long tmpS3 = ml[3];
        long tmpS4 = ml[4];
        long tmpS5 = ml[5];
        long tmpS6 = ml[6];
        long tmpS7 = ml[7];

        for (int r = 0; r < 12; r++) {

            tmpT0 = tmpS0 ^ tmpK0;
            tmpT1 = tmpS1 ^ tmpK1;
            tmpT2 = tmpS2 ^ tmpK2;
            tmpT3 = tmpS3 ^ tmpK3;
            tmpT4 = tmpS4 ^ tmpK4;
            tmpT5 = tmpS5 ^ tmpK5;
            tmpT6 = tmpS6 ^ tmpK6;
            tmpT7 = tmpS7 ^ tmpK7;

            tmpS0 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 56) & 0xFF)];
            tmpS1 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 48) & 0xFF)];
            tmpS2 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 40) & 0xFF)];
            tmpS3 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 32) & 0xFF)];
            tmpS4 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 24) & 0xFF)];
            tmpS5 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 16) & 0xFF)];
            tmpS6 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 8) & 0xFF)];
            tmpS7 = StreebogTables.sbob_sl64[0][(int) (tmpT0 & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) (tmpT1 & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) (tmpT2 & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) (tmpT3 & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) (tmpT4 & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) (tmpT5 & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) (tmpT6 & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) (tmpT7 & 0xFF)];

            tmpT0 = tmpK0 ^ StreebogTables.sbob_rc64[r][0];
            tmpT1 = tmpK1 ^ StreebogTables.sbob_rc64[r][1];
            tmpT2 = tmpK2 ^ StreebogTables.sbob_rc64[r][2];
            tmpT3 = tmpK3 ^ StreebogTables.sbob_rc64[r][3];
            tmpT4 = tmpK4 ^ StreebogTables.sbob_rc64[r][4];
            tmpT5 = tmpK5 ^ StreebogTables.sbob_rc64[r][5];
            tmpT6 = tmpK6 ^ StreebogTables.sbob_rc64[r][6];
            tmpT7 = tmpK7 ^ StreebogTables.sbob_rc64[r][7];

            tmpK0 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 56) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 56) & 0xFF)];
            tmpK1 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 48) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 48) & 0xFF)];
            tmpK2 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 40) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 40) & 0xFF)];
            tmpK3 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 32) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 32) & 0xFF)];
            tmpK4 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 24) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 24) & 0xFF)];
            tmpK5 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 16) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 16) & 0xFF)];
            tmpK6 = StreebogTables.sbob_sl64[0][(int) ((tmpT0 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) ((tmpT1 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) ((tmpT2 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) ((tmpT3 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) ((tmpT4 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) ((tmpT5 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) ((tmpT6 >> 8) & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) ((tmpT7 >> 8) & 0xFF)];
            tmpK7 = StreebogTables.sbob_sl64[0][(int) (tmpT0 & 0xFF)] ^
                    StreebogTables.sbob_sl64[1][(int) (tmpT1 & 0xFF)] ^
                    StreebogTables.sbob_sl64[2][(int) (tmpT2 & 0xFF)] ^
                    StreebogTables.sbob_sl64[3][(int) (tmpT3 & 0xFF)] ^
                    StreebogTables.sbob_sl64[4][(int) (tmpT4 & 0xFF)] ^
                    StreebogTables.sbob_sl64[5][(int) (tmpT5 & 0xFF)] ^
                    StreebogTables.sbob_sl64[6][(int) (tmpT6 & 0xFF)] ^
                    StreebogTables.sbob_sl64[7][(int) (tmpT7 & 0xFF)];
        }

        h[0] = h[0] ^ tmpS0 ^ tmpK0 ^ ml[0];
        h[1] = h[1] ^ tmpS1 ^ tmpK1 ^ ml[1];
        h[2] = h[2] ^ tmpS2 ^ tmpK2 ^ ml[2];
        h[3] = h[3] ^ tmpS3 ^ tmpK3 ^ ml[3];
        h[4] = h[4] ^ tmpS4 ^ tmpK4 ^ ml[4];
        h[5] = h[5] ^ tmpS5 ^ tmpK5 ^ ml[5];
        h[6] = h[6] ^ tmpS6 ^ tmpK6 ^ ml[6];
        h[7] = h[7] ^ tmpS7 ^ tmpK7 ^ ml[7];
    }
}
