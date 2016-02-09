package im.actor.crypto.primitives.streebog;

import im.actor.crypto.primitives.util.Pack;

public class StreebogFastDigest {

    private final int hashLength;

    private long H0;
    private long H1;
    private long H2;
    private long H3;
    private long H4;
    private long H5;
    private long H6;
    private long H7;

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
            H0 = 0x0101010101010101L;
            H1 = 0x0101010101010101L;
            H2 = 0x0101010101010101L;
            H3 = 0x0101010101010101L;
            H4 = 0x0101010101010101L;
            H5 = 0x0101010101010101L;
            H6 = 0x0101010101010101L;
            H7 = 0x0101010101010101L;
        } else {
            H0 = 0L;
            H1 = 0L;
            H2 = 0L;
            H3 = 0L;
            H4 = 0L;
            H5 = 0L;
            H6 = 0L;
            H7 = 0L;
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

        Pack.longToBigEndian(new long[]{H0, H1, H2, H3, H4, H5, H6, H7}, out, offset, hashLength / 8);

        // clear out sensitive stuff
        reset();
    }

    private void streebog_g(byte[] m, long n) {

        // long[] hl = new long[8];
        long[] ml = new long[8];
        // Pack.bigEndianToLong(h, 0, hl);
        Pack.bigEndianToLong(m, 0, ml);

        long tmpT0 = H0;
        long tmpT1 = H1;
        long tmpT2 = H2;
        long tmpT3 = H3;
        long tmpT4 = H4;
        long tmpT5 = H5;
        long tmpT6 = H6;
        long tmpT7 = H7 ^ n;

        int o0 = 256 * 0;
        int o1 = 256 * 1;
        int o2 = 256 * 2;
        int o3 = 256 * 3;
        int o4 = 256 * 4;
        int o5 = 256 * 5;
        int o6 = 256 * 6;
        int o7 = 256 * 7;

        long tmpK0 = sbob_sl64[o0 + (int) ((tmpT0 >> 56) & 0xFF)] ^
                sbob_sl64[o1 + (int) ((tmpT1 >> 56) & 0xFF)] ^
                sbob_sl64[o2 + (int) ((tmpT2 >> 56) & 0xFF)] ^
                sbob_sl64[o3 + (int) ((tmpT3 >> 56) & 0xFF)] ^
                sbob_sl64[o4 + (int) ((tmpT4 >> 56) & 0xFF)] ^
                sbob_sl64[o5 + (int) ((tmpT5 >> 56) & 0xFF)] ^
                sbob_sl64[o6 + (int) ((tmpT6 >> 56) & 0xFF)] ^
                sbob_sl64[o7 + (int) ((tmpT7 >> 56) & 0xFF)];
        long tmpK1 = sbob_sl64[o0 + (int) ((tmpT0 >> 48) & 0xFF)] ^
                sbob_sl64[o1 + (int) ((tmpT1 >> 48) & 0xFF)] ^
                sbob_sl64[o2 + (int) ((tmpT2 >> 48) & 0xFF)] ^
                sbob_sl64[o3 + (int) ((tmpT3 >> 48) & 0xFF)] ^
                sbob_sl64[o4 + (int) ((tmpT4 >> 48) & 0xFF)] ^
                sbob_sl64[o5 + (int) ((tmpT5 >> 48) & 0xFF)] ^
                sbob_sl64[o6 + (int) ((tmpT6 >> 48) & 0xFF)] ^
                sbob_sl64[o7 + (int) ((tmpT7 >> 48) & 0xFF)];
        long tmpK2 = sbob_sl64[o0 + (int) ((tmpT0 >> 40) & 0xFF)] ^
                sbob_sl64[o1 + (int) ((tmpT1 >> 40) & 0xFF)] ^
                sbob_sl64[o2 + (int) ((tmpT2 >> 40) & 0xFF)] ^
                sbob_sl64[o3 + (int) ((tmpT3 >> 40) & 0xFF)] ^
                sbob_sl64[o4 + (int) ((tmpT4 >> 40) & 0xFF)] ^
                sbob_sl64[o5 + (int) ((tmpT5 >> 40) & 0xFF)] ^
                sbob_sl64[o6 + (int) ((tmpT6 >> 40) & 0xFF)] ^
                sbob_sl64[o7 + (int) ((tmpT7 >> 40) & 0xFF)];
        long tmpK3 = sbob_sl64[o0 + (int) ((tmpT0 >> 32) & 0xFF)] ^
                sbob_sl64[o1 + (int) ((tmpT1 >> 32) & 0xFF)] ^
                sbob_sl64[o2 + (int) ((tmpT2 >> 32) & 0xFF)] ^
                sbob_sl64[o3 + (int) ((tmpT3 >> 32) & 0xFF)] ^
                sbob_sl64[o4 + (int) ((tmpT4 >> 32) & 0xFF)] ^
                sbob_sl64[o5 + (int) ((tmpT5 >> 32) & 0xFF)] ^
                sbob_sl64[o6 + (int) ((tmpT6 >> 32) & 0xFF)] ^
                sbob_sl64[o7 + (int) ((tmpT7 >> 32) & 0xFF)];
        long tmpK4 = sbob_sl64[o0 + (int) ((tmpT0 >> 24) & 0xFF)] ^
                sbob_sl64[o1 + (int) ((tmpT1 >> 24) & 0xFF)] ^
                sbob_sl64[o2 + (int) ((tmpT2 >> 24) & 0xFF)] ^
                sbob_sl64[o3 + (int) ((tmpT3 >> 24) & 0xFF)] ^
                sbob_sl64[o4 + (int) ((tmpT4 >> 24) & 0xFF)] ^
                sbob_sl64[o5 + (int) ((tmpT5 >> 24) & 0xFF)] ^
                sbob_sl64[o6 + (int) ((tmpT6 >> 24) & 0xFF)] ^
                sbob_sl64[o7 + (int) ((tmpT7 >> 24) & 0xFF)];
        long tmpK5 = sbob_sl64[o0 + (int) ((tmpT0 >> 16) & 0xFF)] ^
                sbob_sl64[o1 + (int) ((tmpT1 >> 16) & 0xFF)] ^
                sbob_sl64[o2 + (int) ((tmpT2 >> 16) & 0xFF)] ^
                sbob_sl64[o3 + (int) ((tmpT3 >> 16) & 0xFF)] ^
                sbob_sl64[o4 + (int) ((tmpT4 >> 16) & 0xFF)] ^
                sbob_sl64[o5 + (int) ((tmpT5 >> 16) & 0xFF)] ^
                sbob_sl64[o6 + (int) ((tmpT6 >> 16) & 0xFF)] ^
                sbob_sl64[o7 + (int) ((tmpT7 >> 16) & 0xFF)];
        long tmpK6 = sbob_sl64[o0 + (int) ((tmpT0 >> 8) & 0xFF)] ^
                sbob_sl64[o1 + (int) ((tmpT1 >> 8) & 0xFF)] ^
                sbob_sl64[o2 + (int) ((tmpT2 >> 8) & 0xFF)] ^
                sbob_sl64[o3 + (int) ((tmpT3 >> 8) & 0xFF)] ^
                sbob_sl64[o4 + (int) ((tmpT4 >> 8) & 0xFF)] ^
                sbob_sl64[o5 + (int) ((tmpT5 >> 8) & 0xFF)] ^
                sbob_sl64[o6 + (int) ((tmpT6 >> 8) & 0xFF)] ^
                sbob_sl64[o7 + (int) ((tmpT7 >> 8) & 0xFF)];
        long tmpK7 = sbob_sl64[o0 + (int) (tmpT0 & 0xFF)] ^
                sbob_sl64[o1 + (int) (tmpT1 & 0xFF)] ^
                sbob_sl64[o2 + (int) (tmpT2 & 0xFF)] ^
                sbob_sl64[o3 + (int) (tmpT3 & 0xFF)] ^
                sbob_sl64[o4 + (int) (tmpT4 & 0xFF)] ^
                sbob_sl64[o5 + (int) (tmpT5 & 0xFF)] ^
                sbob_sl64[o6 + (int) (tmpT6 & 0xFF)] ^
                sbob_sl64[o7 + (int) (tmpT7 & 0xFF)];


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

            tmpS0 = sbob_sl64[o0 + (int) ((tmpT0 >> 56) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 56) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 56) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 56) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 56) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 56) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 56) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 56) & 0xFF)];
            tmpS1 = sbob_sl64[o0 + (int) ((tmpT0 >> 48) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 48) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 48) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 48) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 48) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 48) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 48) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 48) & 0xFF)];
            tmpS2 = sbob_sl64[o0 + (int) ((tmpT0 >> 40) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 40) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 40) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 40) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 40) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 40) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 40) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 40) & 0xFF)];
            tmpS3 = sbob_sl64[o0 + (int) ((tmpT0 >> 32) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 32) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 32) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 32) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 32) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 32) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 32) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 32) & 0xFF)];
            tmpS4 = sbob_sl64[o0 + (int) ((tmpT0 >> 24) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 24) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 24) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 24) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 24) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 24) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 24) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 24) & 0xFF)];
            tmpS5 = sbob_sl64[o0 + (int) ((tmpT0 >> 16) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 16) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 16) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 16) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 16) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 16) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 16) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 16) & 0xFF)];
            tmpS6 = sbob_sl64[o0 + (int) ((tmpT0 >> 8) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 8) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 8) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 8) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 8) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 8) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 8) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 8) & 0xFF)];
            tmpS7 = sbob_sl64[o0 + (int) (tmpT0 & 0xFF)] ^
                    sbob_sl64[o1 + (int) (tmpT1 & 0xFF)] ^
                    sbob_sl64[o2 + (int) (tmpT2 & 0xFF)] ^
                    sbob_sl64[o3 + (int) (tmpT3 & 0xFF)] ^
                    sbob_sl64[o4 + (int) (tmpT4 & 0xFF)] ^
                    sbob_sl64[o5 + (int) (tmpT5 & 0xFF)] ^
                    sbob_sl64[o6 + (int) (tmpT6 & 0xFF)] ^
                    sbob_sl64[o7 + (int) (tmpT7 & 0xFF)];

            tmpT0 = tmpK0 ^ sbob_rc64[r][0];
            tmpT1 = tmpK1 ^ sbob_rc64[r][1];
            tmpT2 = tmpK2 ^ sbob_rc64[r][2];
            tmpT3 = tmpK3 ^ sbob_rc64[r][3];
            tmpT4 = tmpK4 ^ sbob_rc64[r][4];
            tmpT5 = tmpK5 ^ sbob_rc64[r][5];
            tmpT6 = tmpK6 ^ sbob_rc64[r][6];
            tmpT7 = tmpK7 ^ sbob_rc64[r][7];

            tmpK0 = sbob_sl64[o0 + (int) ((tmpT0 >> 56) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 56) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 56) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 56) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 56) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 56) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 56) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 56) & 0xFF)];
            tmpK1 = sbob_sl64[o0 + (int) ((tmpT0 >> 48) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 48) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 48) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 48) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 48) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 48) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 48) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 48) & 0xFF)];
            tmpK2 = sbob_sl64[o0 + (int) ((tmpT0 >> 40) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 40) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 40) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 40) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 40) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 40) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 40) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 40) & 0xFF)];
            tmpK3 = sbob_sl64[o0 + (int) ((tmpT0 >> 32) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 32) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 32) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 32) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 32) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 32) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 32) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 32) & 0xFF)];
            tmpK4 = sbob_sl64[o0 + (int) ((tmpT0 >> 24) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 24) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 24) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 24) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 24) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 24) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 24) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 24) & 0xFF)];
            tmpK5 = sbob_sl64[o0 + (int) ((tmpT0 >> 16) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 16) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 16) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 16) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 16) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 16) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 16) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 16) & 0xFF)];
            tmpK6 = sbob_sl64[o0 + (int) ((tmpT0 >> 8) & 0xFF)] ^
                    sbob_sl64[o1 + (int) ((tmpT1 >> 8) & 0xFF)] ^
                    sbob_sl64[o2 + (int) ((tmpT2 >> 8) & 0xFF)] ^
                    sbob_sl64[o3 + (int) ((tmpT3 >> 8) & 0xFF)] ^
                    sbob_sl64[o4 + (int) ((tmpT4 >> 8) & 0xFF)] ^
                    sbob_sl64[o5 + (int) ((tmpT5 >> 8) & 0xFF)] ^
                    sbob_sl64[o6 + (int) ((tmpT6 >> 8) & 0xFF)] ^
                    sbob_sl64[o7 + (int) ((tmpT7 >> 8) & 0xFF)];
            tmpK7 = sbob_sl64[o0 + (int) (tmpT0 & 0xFF)] ^
                    sbob_sl64[o1 + (int) (tmpT1 & 0xFF)] ^
                    sbob_sl64[o2 + (int) (tmpT2 & 0xFF)] ^
                    sbob_sl64[o3 + (int) (tmpT3 & 0xFF)] ^
                    sbob_sl64[o4 + (int) (tmpT4 & 0xFF)] ^
                    sbob_sl64[o5 + (int) (tmpT5 & 0xFF)] ^
                    sbob_sl64[o6 + (int) (tmpT6 & 0xFF)] ^
                    sbob_sl64[o7 + (int) (tmpT7 & 0xFF)];
        }

        H0 = H0 ^ tmpS0 ^ tmpK0 ^ ml[0];
        H1 = H1 ^ tmpS1 ^ tmpK1 ^ ml[1];
        H2 = H2 ^ tmpS2 ^ tmpK2 ^ ml[2];
        H3 = H3 ^ tmpS3 ^ tmpK3 ^ ml[3];
        H4 = H4 ^ tmpS4 ^ tmpK4 ^ ml[4];
        H5 = H5 ^ tmpS5 ^ tmpK5 ^ ml[5];
        H6 = H6 ^ tmpS6 ^ tmpK6 ^ ml[6];
        H7 = H7 ^ tmpS7 ^ tmpK7 ^ ml[7];
    }


    private static final long[] sbob_sl64 = new long[]{

            0xE63F55CE97C331D0L, 0x25B506B0015BBA16L,     // 0:00
            0xC8706E29E6AD9BA8L, 0x5B43D3775D521F6AL,     // 0:02
            0x0BFA3D577035106EL, 0xAB95FC172AFB0E66L,     // 0:04
            0xF64B63979E7A3276L, 0xF58B4562649DAD4BL,     // 0:06
            0x48F7C3DBAE0C83F1L, 0xFF31916642F5C8C5L,     // 0:08
            0xCBB048DC1C4A0495L, 0x66B8F83CDF622989L,     // 0:0A
            0x35C130E908E2B9B0L, 0x7C761A61F0B34FA1L,     // 0:0C
            0x3601161CF205268DL, 0x9E54CCFE2219B7D6L,     // 0:0E
            0x8B7D90A538940837L, 0x9CD403588EA35D0BL,     // 0:10
            0xBC3C6FEA9CCC5B5AL, 0xE5FF733B6D24AEEDL,     // 0:12
            0xCEED22DE0F7EB8D2L, 0xEC8581CAB1AB545EL,     // 0:14
            0xB96105E88FF8E71DL, 0x8CA03501871A5EADL,     // 0:16
            0x76CCCE65D6DB2A2FL, 0x5883F582A7B58057L,     // 0:18
            0x3F7BE4ED2E8ADC3EL, 0x0FE7BE06355CD9C9L,     // 0:1A
            0xEE054E6C1D11BE83L, 0x1074365909B903A6L,     // 0:1C
            0x5DDE9F80B4813C10L, 0x4A770C7D02B6692CL,     // 0:1E
            0x5379C8D5D7809039L, 0xB4067448161ED409L,     // 0:20
            0x5F5E5026183BD6CDL, 0xE898029BF4C29DF9L,     // 0:22
            0x7FB63C940A54D09CL, 0xC5171F897F4BA8BCL,     // 0:24
            0xA6F28DB7B31D3D72L, 0x2E4F3BE7716EAA78L,     // 0:26
            0x0D6771A099E63314L, 0x82076254E41BF284L,     // 0:28
            0x2F0FD2B42733DF98L, 0x5C9E76D3E2DC49F0L,     // 0:2A
            0x7AEB569619606CDBL, 0x83478B07B2468764L,     // 0:2C
            0xCFADCB8D5923CD32L, 0x85DAC7F05B95A41EL,     // 0:2E
            0xB5469D1B4043A1E9L, 0xB821ECBBD9A592FDL,     // 0:30
            0x1B8E0B0E798C13C8L, 0x62A57B6D9A0BE02EL,     // 0:32
            0xFCF1B793B81257F8L, 0x9D94EA0BD8FE28EBL,     // 0:34
            0x4CEA408AEB654A56L, 0x23284A47E888996CL,     // 0:36
            0x2D8F1D128B893545L, 0xF4CBAC3132C0D8ABL,     // 0:38
            0xBD7C86B9CA912EBAL, 0x3A268EEF3DBE6079L,     // 0:3A
            0xF0D62F6077A9110CL, 0x2735C916ADE150CBL,     // 0:3C
            0x89FD5F03942EE2EAL, 0x1ACEE25D2FD16628L,     // 0:3E
            0x90F39BAB41181BFFL, 0x430DFE8CDE39939FL,     // 0:40
            0xF70B8AC4C8274796L, 0x1C53AEAAC6024552L,     // 0:42
            0x13B410ACF35E9C9BL, 0xA532AB4249FAA24FL,     // 0:44
            0x2B1251E5625A163FL, 0xD7E3E676DA4841C7L,     // 0:46
            0xA7B264E4E5404892L, 0xDA8497D643AE72D3L,     // 0:48
            0x861AE105A1723B23L, 0x38A6414991048AA4L,     // 0:4A
            0x6578DEC92585B6B4L, 0x0280CFA6ACBAEADDL,     // 0:4C
            0x88BDB650C273970AL, 0x9333BD5EBBFF84C2L,     // 0:4E
            0x4E6A8F2C47DFA08BL, 0x321C954DB76CEF2AL,     // 0:50
            0x418D312A72837942L, 0xB29B38BFFFCDF773L,     // 0:52
            0x6C022C38F90A4C07L, 0x5A033A240B0F6A8AL,     // 0:54
            0x1F93885F3CE5DA6FL, 0xC38A537E96988BC6L,     // 0:56
            0x39E6A81AC759FF44L, 0x29929E43CEE0FCE2L,     // 0:58
            0x40CDD87924DE0CA2L, 0xE9D8EBC8A29FE819L,     // 0:5A
            0x0C2798F3CFBB46F4L, 0x55E484223E53B343L,     // 0:5C
            0x4650948ECD0D2FD8L, 0x20E86CB2126F0651L,     // 0:5E
            0x6D42C56BAF5739E7L, 0xA06FC1405ACE1E08L,     // 0:60
            0x7BABBFC54F3D193BL, 0x424D17DF8864E67FL,     // 0:62
            0xD8045870EF14980EL, 0xC6D7397C85AC3781L,     // 0:64
            0x21A885E1443273B1L, 0x67F8116F893F5C69L,     // 0:66
            0x24F5EFE35706CFF6L, 0xD56329D076F2AB1AL,     // 0:68
            0x5E1EB9754E66A32DL, 0x28D2771098BD8902L,     // 0:6A
            0x8F6013F47DFDC190L, 0x17A993FDB637553CL,     // 0:6C
            0xE0A219397E1012AAL, 0x786B9930B5DA8606L,     // 0:6E
            0x6E82E39E55B0A6DAL, 0x875A0856F72F4EC3L,     // 0:70
            0x3741FF4FA458536DL, 0xAC4859B3957558FCL,     // 0:72
            0x7EF6D5C75C09A57CL, 0xC04A758B6C7F14FBL,     // 0:74
            0xF9ACDD91AB26EBBFL, 0x7391A467C5EF9668L,     // 0:76
            0x335C7C1EE1319ACAL, 0xA91533B18641E4BBL,     // 0:78
            0xE4BF9A683B79DB0DL, 0x8E20FAA72BA0B470L,     // 0:7A
            0x51F907737B3A7AE4L, 0x2268A314BED5EC8CL,     // 0:7C
            0xD944B123B949EDEEL, 0x31DCB3B84D8B7017L,     // 0:7E
            0xD3FE65279F218860L, 0x097AF2F1DC8FFAB3L,     // 0:80
            0x9B09A6FC312D0B91L, 0xCC6DED78A3C4520FL,     // 0:82
            0x3481D9BA5EBFCC50L, 0x4F2A667F1182D56BL,     // 0:84
            0xDFD9FDD4509ACE94L, 0x26752045FBBC252BL,     // 0:86
            0xBFFC491F662BC467L, 0xDD593272FC202449L,     // 0:88
            0x3CBBC218D46D4303L, 0x91B372F817456E1FL,     // 0:8A
            0x681FAF69BC6385A0L, 0xB686BBEEBAA43ED4L,     // 0:8C
            0x1469B5084CD0CA01L, 0x98C98009CBCA94ACL,     // 0:8E
            0x6438379A73D8C354L, 0xC2CABA2DC0C5FE26L,     // 0:90
            0x3E3B0DBE78D7A9DEL, 0x50B9EE202D670F04L,     // 0:92
            0x4590B27B37EAB0E5L, 0x6025B4CB36B10AF3L,     // 0:94
            0xFB2C1237079C0162L, 0xA12F28130C936BE8L,     // 0:96
            0x4B37E52E54EB1CCCL, 0x083A1BA28AD28F53L,     // 0:98
            0xC10A9CD83A22611BL, 0x9F1425AD7444C236L,     // 0:9A
            0x069D4CF7E9D3237AL, 0xEDC56899E7F621BEL,     // 0:9C
            0x778C273680865FCFL, 0x309C5AEB1BD605F7L,     // 0:9E
            0x8DE0DC52D1472B4DL, 0xF8EC34C2FD7B9E5FL,     // 0:A0
            0xEA18CD3D58787724L, 0xAAD515447CA67B86L,     // 0:A2
            0x9989695A9D97E14CL, 0x0000000000000000L,     // 0:A4
            0xF196C63321F464ECL, 0x71116BC169557CB5L,     // 0:A6
            0xAF887F466F92C7C1L, 0x972E3E0FFE964D65L,     // 0:A8
            0x190EC4A8D536F915L, 0x95AEF1A9522CA7B8L,     // 0:AA
            0xDC19DB21AA7D51A9L, 0x94EE18FA0471D258L,     // 0:AC
            0x8087ADF248A11859L, 0xC457F6DA2916DD5CL,     // 0:AE
            0xFA6CFB6451C17482L, 0xF256E0C6DB13FBD1L,     // 0:B0
            0x6A9F60CF10D96F7DL, 0x4DAAA9D9BD383FB6L,     // 0:B2
            0x03C026F5FAE79F3DL, 0xDE99148706C7BB74L,     // 0:B4
            0x2A52B8B6340763DFL, 0x6FC20ACD03EDD33AL,     // 0:B6
            0xD423C08320AFDEFAL, 0xBBE1CA4E23420DC0L,     // 0:B8
            0x966ED75CA8CB3885L, 0xEB58246E0E2502C4L,     // 0:BA
            0x055D6A021334BC47L, 0xA47242111FA7D7AFL,     // 0:BC
            0xE3623FCC84F78D97L, 0x81C744A11EFC6DB9L,     // 0:BE
            0xAEC8961539CFB221L, 0xF31609958D4E8E31L,     // 0:C0
            0x63E5923ECC5695CEL, 0x47107DDD9B505A38L,     // 0:C2
            0xA3AFE7B5A0298135L, 0x792B7063E387F3E6L,     // 0:C4
            0x0140E953565D75E0L, 0x12F4F9FFA503E97BL,     // 0:C6
            0x750CE8902C3CB512L, 0xDBC47E8515F30733L,     // 0:C8
            0x1ED3610C6AB8AF8FL, 0x5239218681DDE5D9L,     // 0:CA
            0xE222D69FD2AAF877L, 0xFE71783514A8BD25L,     // 0:CC
            0xCAF0A18F4A177175L, 0x61655D9860EC7F13L,     // 0:CE
            0xE77FBC9DC19E4430L, 0x2CCFF441DDD440A5L,     // 0:D0
            0x16E97AAEE06A20DCL, 0xA855DAE2D01C915BL,     // 0:D2
            0x1D1347F9905F30B2L, 0xB7C652BDECF94B34L,     // 0:D4
            0xD03E43D265C6175DL, 0xFDB15EC0EE4F2218L,     // 0:D6
            0x57644B8492E9599EL, 0x07DDA5A4BF8E569AL,     // 0:D8
            0x54A46D71680EC6A3L, 0x5624A2D7C4B42C7EL,     // 0:DA
            0xBEBCA04C3076B187L, 0x7D36F332A6EE3A41L,     // 0:DC
            0x3B6667BC6BE31599L, 0x695F463AEA3EF040L,     // 0:DE
            0xAD08B0E0C3282D1CL, 0xB15B1E4A052A684EL,     // 0:E0
            0x44D05B2861B7C505L, 0x15295C5B1A8DBFE1L,     // 0:E2
            0x744C01C37A61C0F2L, 0x59C31CD1F1E8F5B7L,     // 0:E4
            0xEF45A73F4B4CCB63L, 0x6BDF899C46841A9DL,     // 0:E6
            0x3DFB2B4B823036E3L, 0xA2EF0EE6F674F4D5L,     // 0:E8
            0x184E2DFB836B8CF5L, 0x1134DF0A5FE47646L,     // 0:EA
            0xBAA1231D751F7820L, 0xD17EAA81339B62BDL,     // 0:EC
            0xB01BF71953771DAEL, 0x849A2EA30DC8D1FEL,     // 0:EE
            0x705182923F080955L, 0x0EA757556301AC29L,     // 0:F0
            0x041D83514569C9A7L, 0x0ABAD4042668658EL,     // 0:F2
            0x49B72A88F851F611L, 0x8A3D79F66EC97DD7L,     // 0:F4
            0xCD2D042BF59927EFL, 0xC930877AB0F0EE48L,     // 0:F6
            0x9273540DEDA2F122L, 0xC797D02FD3F14261L,     // 0:F8
            0xE1E2F06A284D674AL, 0xD2BE8C74C97CFD80L,     // 0:FA
            0x9A494FAF67707E71L, 0xB3DBD1ECA9908293L,     // 0:FC
            0x72D14D3493B2E388L, 0xD6A30F258C153427L      // 0:FE
            ,
            0xC3407DFC2DE6377EL, 0x5B9E93EEA4256F77L,     // 1:00
            0xADB58FDD50C845E0L, 0x5219FF11A75BED86L,     // 1:02
            0x356B61CFD90B1DE9L, 0xFB8F406E25ABE037L,     // 1:04
            0x7A5A0231C0F60796L, 0x9D3CD216E1F5020BL,     // 1:06
            0x0C6550FB6B48D8F3L, 0xF57508C427FF1C62L,     // 1:08
            0x4AD35FFA71CB407DL, 0x6290A2DA1666AA6DL,     // 1:0A
            0xE284EC2349355F9FL, 0xB3C307C53D7C84ECL,     // 1:0C
            0x05E23C0468365A02L, 0x190BAC4D6C9EBFA8L,     // 1:0E
            0x94BBBEE9E28B80FAL, 0xA34FC777529CB9B5L,     // 1:10
            0xCC7B39F095BCD978L, 0x2426ADDB0CE532E3L,     // 1:12
            0x7E79329312CE4FC7L, 0xAB09A72EEBEC2917L,     // 1:14
            0xF8D15499F6B9D6C2L, 0x1A55B8BABF8C895DL,     // 1:16
            0xDB8ADD17FB769A85L, 0xB57F2F368658E81BL,     // 1:18
            0x8ACD36F18F3F41F6L, 0x5CE3B7BBA50F11D3L,     // 1:1A
            0x114DCC14D5EE2F0AL, 0xB91A7FCDED1030E8L,     // 1:1C
            0x81D5425FE55DE7A1L, 0xB6213BC1554ADEEEL,     // 1:1E
            0x80144EF95F53F5F2L, 0x1E7688186DB4C10CL,     // 1:20
            0x3B912965DB5FE1BCL, 0xC281715A97E8252DL,     // 1:22
            0x54A5D7E21C7F8171L, 0x4B12535CCBC5522EL,     // 1:24
            0x1D289CEFBEA6F7F9L, 0x6EF5F2217D2E729EL,     // 1:26
            0xE6A7DC819B0D17CEL, 0x1B94B41C05829B0EL,     // 1:28
            0x33D7493C622F711EL, 0xDCF7F942FA5CE421L,     // 1:2A
            0x600FBA8B7F7A8ECBL, 0x46B60F011A83988EL,     // 1:2C
            0x235B898E0DCF4C47L, 0x957AB24F588592A9L,     // 1:2E
            0x4354330572B5C28CL, 0xA5F3EF84E9B8D542L,     // 1:30
            0x8C711E02341B2D01L, 0x0B1874AE6A62A657L,     // 1:32
            0x1213D8E306FC19FFL, 0xFE6D7C6A4D9DBA35L,     // 1:34
            0x65ED868F174CD4C9L, 0x88522EA0E6236550L,     // 1:36
            0x899322065C2D7703L, 0xC01E690BFEF4018BL,     // 1:38
            0x915982ED8ABDDAF8L, 0xBE675B98EC3A4E4CL,     // 1:3A
            0xA996BF7F82F00DB1L, 0xE1DAF8D49A27696AL,     // 1:3C
            0x2EFFD5D3DC8986E7L, 0xD153A51F2B1A2E81L,     // 1:3E
            0x18CAA0EBD690ADFBL, 0x390E3134B243C51AL,     // 1:40
            0x2778B92CDFF70416L, 0x029F1851691C24A6L,     // 1:42
            0x5E7CAFEACC133575L, 0xFA4E4CC89FA5F264L,     // 1:44
            0x5A5F9F481E2B7D24L, 0x484C47AB18D764DBL,     // 1:46
            0x400A27F2A1A7F479L, 0xAEEB9B2A83DA7315L,     // 1:48
            0x721C626879869734L, 0x042330A2D2384851L,     // 1:4A
            0x85F672FD3765AFF0L, 0xBA446B3A3E02061DL,     // 1:4C
            0x73DD6ECEC3888567L, 0xFFAC70CCF793A866L,     // 1:4E
            0xDFA9EDB5294ED2D4L, 0x6C6AEA7014325638L,     // 1:50
            0x834A5A0E8C41C307L, 0xCDBA35562FB2CB2BL,     // 1:52
            0x0AD97808D06CB404L, 0x0F3B440CB85AEE06L,     // 1:54
            0xE5F9C876481F213BL, 0x98DEEE1289C35809L,     // 1:56
            0x59018BBFCD394BD1L, 0xE01BF47220297B39L,     // 1:58
            0xDE68E1139340C087L, 0x9FA3CA4788E926ADL,     // 1:5A
            0xBB85679C840C144EL, 0x53D8F3B71D55FFD5L,     // 1:5C
            0x0DA45C5DD146CAA0L, 0x6F34FE87C72060CDL,     // 1:5E
            0x57FBC315CF6DB784L, 0xCEE421A1FCA0FDDEL,     // 1:60
            0x3D2D0196607B8D4BL, 0x642C8A29AD42C69AL,     // 1:62
            0x14AFF010BDD87508L, 0xAC74837BEAC657B3L,     // 1:64
            0x3216459AD821634DL, 0x3FB219C70967A9EDL,     // 1:66
            0x06BC28F3BB246CF7L, 0xF2082C9126D562C6L,     // 1:68
            0x66B39278C45EE23CL, 0xBD394F6F3F2878B9L,     // 1:6A
            0xFD33689D9E8F8CC0L, 0x37F4799EB017394FL,     // 1:6C
            0x108CC0B26FE03D59L, 0xDA4BD1B1417888D6L,     // 1:6E
            0xB09D1332EE6EB219L, 0x2F3ED975668794B4L,     // 1:70
            0x58C0871977375982L, 0x7561463D78ACE990L,     // 1:72
            0x09876CFF037E82F1L, 0x7FB83E35A8C05D94L,     // 1:74
            0x26B9B58A65F91645L, 0xEF20B07E9873953FL,     // 1:76
            0x3148516D0B3355B8L, 0x41CB2B541BA9E62AL,     // 1:78
            0x790416C613E43163L, 0xA011D380818E8F40L,     // 1:7A
            0x3A5025C36151F3EFL, 0xD57095BDF92266D0L,     // 1:7C
            0x498D4B0DA2D97688L, 0x8B0C3A57353153A5L,     // 1:7E
            0x21C491DF64D368E1L, 0x8F2F0AF5E7091BF4L,     // 1:80
            0x2DA1C1240F9BB012L, 0xC43D59A92CCC49DAL,     // 1:82
            0xBFA6573E56345C1FL, 0x828B56A8364FD154L,     // 1:84
            0x9A41F643E0DF7CAFL, 0xBCF843C985266AEAL,     // 1:86
            0x2B1DE9D7B4BFDCE5L, 0x20059D79DEDD7AB2L,     // 1:88
            0x6DABE6D6AE3C446BL, 0x45E81BF6C991AE7BL,     // 1:8A
            0x6351AE7CAC68B83EL, 0xA432E32253B6C711L,     // 1:8C
            0xD092A9B991143CD2L, 0xCAC711032E98B58FL,     // 1:8E
            0xD8D4C9E02864AC70L, 0xC5FC550F96C25B89L,     // 1:90
            0xD7EF8DEC903E4276L, 0x67729EDE7E50F06FL,     // 1:92
            0xEAC28C7AF045CF3DL, 0xB15C1F945460A04AL,     // 1:94
            0x9CFDDEB05BFB1058L, 0x93C69ABCE3A1FE5EL,     // 1:96
            0xEB0380DC4A4BDD6EL, 0xD20DB1E8F8081874L,     // 1:98
            0x229A8528B7C15E14L, 0x44291750739FBC28L,     // 1:9A
            0xD3CCBD4E42060A27L, 0xF62B1C33F4ED2A97L,     // 1:9C
            0x86A8660AE4779905L, 0xD62E814A2A305025L,     // 1:9E
            0x477703A7A08D8ADDL, 0x7B9B0E977AF815C5L,     // 1:A0
            0x78C51A60A9EA2330L, 0xA6ADFB733AAAE3B7L,     // 1:A2
            0x97E5AA1E3199B60FL, 0x0000000000000000L,     // 1:A4
            0xF4B404629DF10E31L, 0x5564DB44A6719322L,     // 1:A6
            0x9207961A59AFEC0DL, 0x9624A6B88B97A45CL,     // 1:A8
            0x363575380A192B1CL, 0x2C60CD82B595A241L,     // 1:AA
            0x7D272664C1DC7932L, 0x7142769FAA94A1C1L,     // 1:AC
            0xA1D0DF263B809D13L, 0x1630E841D4C451AEL,     // 1:AE
            0xC1DF65AD44FA13D8L, 0x13D2D445BCF20BACL,     // 1:B0
            0xD915C546926ABE23L, 0x38CF3D92084DD749L,     // 1:B2
            0xE766D0272103059DL, 0xC7634D5EFFDE7F2FL,     // 1:B4
            0x077D2455012A7EA4L, 0xEDBFA82FF16FB199L,     // 1:B6
            0xAF2A978C39D46146L, 0x42953FA3C8BBD0DFL,     // 1:B8
            0xCB061DA59496A7DCL, 0x25E7A17DB6EB20B0L,     // 1:BA
            0x34AA6D6963050FBAL, 0xA76CF7D580A4F1E4L,     // 1:BC
            0xF7EA10954EE338C4L, 0xFCF2643B24819E93L,     // 1:BE
            0xCF252D0746AEEF8DL, 0x4EF06F58A3F3082CL,     // 1:C0
            0x563ACFB37563A5D7L, 0x5086E740CE47C920L,     // 1:C2
            0x2982F186DDA3F843L, 0x87696AAC5E798B56L,     // 1:C4
            0x5D22BB1D1F010380L, 0x035E14F7D31236F5L,     // 1:C6
            0x3CEC0D30DA759F18L, 0xF3C920379CDB7095L,     // 1:C8
            0xB8DB736B571E22BBL, 0xDD36F5E44052F672L,     // 1:CA
            0xAAC8AB8851E23B44L, 0xA857B3D938FE1FE2L,     // 1:CC
            0x17F1E4E76ECA43FDL, 0xEC7EA4894B61A3CAL,     // 1:CE
            0x9E62C6E132E734FEL, 0xD4B1991B432C7483L,     // 1:D0
            0x6AD6C283AF163ACFL, 0x1CE9904904A8E5AAL,     // 1:D2
            0x5FBDA34C761D2726L, 0xF910583F4CB7C491L,     // 1:D4
            0xC6A241F845D06D7CL, 0x4F3163FE19FD1A7FL,     // 1:D6
            0xE99C988D2357F9C8L, 0x8EEE06535D0709A7L,     // 1:D8
            0x0EFA48AA0254FC55L, 0xB4BE23903C56FA48L,     // 1:DA
            0x763F52CAABBEDF65L, 0xEEE1BCD8227D876CL,     // 1:DC
            0xE345E085F33B4DCCL, 0x3E731561B369BBBEL,     // 1:DE
            0x2843FD2067ADEA10L, 0x2ADCE5710EB1CEB6L,     // 1:E0
            0xB7E03767EF44CCBDL, 0x8DB012A48E153F52L,     // 1:E2
            0x61CEB62DC5749C98L, 0xE85D942B9959EB9BL,     // 1:E4
            0x4C6F7709CAEF2C8AL, 0x84377E5B8D6BBDA3L,     // 1:E6
            0x30895DCBB13D47EBL, 0x74A04A9BC2A2FBC3L,     // 1:E8
            0x6B17CE251518289CL, 0xE438C4D0F2113368L,     // 1:EA
            0x1FB784BED7BAD35FL, 0x9B80FAE55AD16EFCL,     // 1:EC
            0x77FE5E6C11B0CD36L, 0xC858095247849129L,     // 1:EE
            0x08466059B97090A2L, 0x01C10CA6BA0E1253L,     // 1:F0
            0x6988D6747C040C3AL, 0x6849DAD2C60A1E69L,     // 1:F2
            0x5147EBE67449DB73L, 0xC99905F4FD8A837AL,     // 1:F4
            0x991FE2B433CD4A5AL, 0xF09734C04FC94660L,     // 1:F6
            0xA28ECBD1E892ABE6L, 0xF1563866F5C75433L,     // 1:F8
            0x4DAE7BAF70E13ED9L, 0x7CE62AC27BD26B61L,     // 1:FA
            0x70837A39109AB392L, 0x90988E4B30B3C8ABL,     // 1:FC
            0xB2020B63877296BFL, 0x156EFCB607D6675BL      // 1:FE
            ,
            0x6D6AE04668A9B08AL, 0x3AB3F04B0BE8C743L,     // 2:00
            0xE51E166B54B3C908L, 0xBE90A9EB35C2F139L,     // 2:02
            0xB2C7066637F2BEC1L, 0xAA6945613392202CL,     // 2:04
            0x9A28C36F3B5201EBL, 0xDDCE5A93AB536994L,     // 2:06
            0x0E34133EF6382827L, 0x52A02BA1EC55048BL,     // 2:08
            0xA2F88F97C4B2A177L, 0x8640E513CA2251A5L,     // 2:0A
            0xCDF1D36258137622L, 0xFE6CB708DEDF8DDBL,     // 2:0C
            0x8A174A9EC8121E5DL, 0x679896036B81560EL,     // 2:0E
            0x59ED033395795FEEL, 0x1DD778AB8B74EDAFL,     // 2:10
            0xEE533EF92D9F926DL, 0x2A8C79BAF8A8D8F5L,     // 2:12
            0x6BCF398E69B119F6L, 0xE20491742FAFDD95L,     // 2:14
            0x276488E0809C2AECL, 0xEA955B82D88F5CCEL,     // 2:16
            0x7102C63A99D9E0C4L, 0xF9763017A5C39946L,     // 2:18
            0x429FA2501F151B3DL, 0x4659C72BEA05D59EL,     // 2:1A
            0x984B7FDCCF5A6634L, 0xF742232953FBB161L,     // 2:1C
            0x3041860E08C021C7L, 0x747BFD9616CD9386L,     // 2:1E
            0x4BB1367192312787L, 0x1B72A1638A6C44D3L,     // 2:20
            0x4A0E68A6E8359A66L, 0x169A5039F258B6CAL,     // 2:22
            0xB98A2EF44EDEE5A4L, 0xD9083FE85E43A737L,     // 2:24
            0x967F6CE239624E13L, 0x8874F62D3C1A7982L,     // 2:26
            0x3C1629830AF06E3FL, 0x9165EBFD427E5A8EL,     // 2:28
            0xB5DD81794CEEAA5CL, 0x0DE8F15A7834F219L,     // 2:2A
            0x70BD98EDE3DD5D25L, 0xACCC9CA9328A8950L,     // 2:2C
            0x56664EDA1945CA28L, 0x221DB34C0F8859AEL,     // 2:2E
            0x26DBD637FA98970DL, 0x1ACDFFB4F068F932L,     // 2:30
            0x4585254F64090FA0L, 0x72DE245E17D53AFAL,     // 2:32
            0x1546B25D7C546CF4L, 0x207E0FFFFB803E71L,     // 2:34
            0xFAAAD2732BCF4378L, 0xB462DFAE36EA17BDL,     // 2:36
            0xCF926FD1AC1B11FDL, 0xE0672DC7DBA7BA4AL,     // 2:38
            0xD3FA49AD5D6B41B3L, 0x8BA81449B216A3BCL,     // 2:3A
            0x14F9EC8A0650D115L, 0x40FC1EE3EB1D7CE2L,     // 2:3C
            0x23A2ED9B758CE44FL, 0x782C521B14FDDC7EL,     // 2:3E
            0x1C68267CF170504EL, 0xBCF31558C1CA96E6L,     // 2:40
            0xA781B43B4BA6D235L, 0xF6FD7DFE29FF0C80L,     // 2:42
            0xB0A4BAD5C3FAD91EL, 0xD199F51EA963266CL,     // 2:44
            0x414340349119C103L, 0x5405F269ED4DADF7L,     // 2:46
            0xABD61BB649969DCDL, 0x6813DBEAE7BDC3C8L,     // 2:48
            0x65FB2AB09F8931D1L, 0xF1E7FAE152E3181DL,     // 2:4A
            0xC1A67CEF5A2339DAL, 0x7A4FEEA8E0F5BBA1L,     // 2:4C
            0x1E0B9ACF05783791L, 0x5B8EBF8061713831L,     // 2:4E
            0x80E53CDBCB3AF8D9L, 0x7E898BD315E57502L,     // 2:50
            0xC6BCFBF0213F2D47L, 0x95A38E86B76E942DL,     // 2:52
            0x092E94218D243CBAL, 0x8339DEBF453622E7L,     // 2:54
            0xB11BE402B9FE64FFL, 0x57D9100D634177C9L,     // 2:56
            0xCC4E8DB52217CBC3L, 0x3B0CAE9C71EC7AA2L,     // 2:58
            0xFB158CA451CBFE99L, 0x2B33276D82AC6514L,     // 2:5A
            0x01BF5ED77A04BDE1L, 0xC5601994AF33F779L,     // 2:5C
            0x75C4A3416CC92E67L, 0xF3844652A6EB7FC2L,     // 2:5E
            0x3487E375FDD0EF64L, 0x18AE430704609EEDL,     // 2:60
            0x4D14EFB993298EFBL, 0x815A620CB13E4538L,     // 2:62
            0x125C354207487869L, 0x9EEEA614CE42CF48L,     // 2:64
            0xCE2D3106D61FAC1CL, 0xBBE99247BAD6827BL,     // 2:66
            0x071A871F7B1C149DL, 0x2E4A1CC10DB81656L,     // 2:68
            0x77A71FF298C149B8L, 0x06A5D9C80118A97CL,     // 2:6A
            0xAD73C27E488E34B1L, 0x443A7B981E0DB241L,     // 2:6C
            0xE3BBCFA355AB6074L, 0x0AF276450328E684L,     // 2:6E
            0x73617A896DD1871BL, 0x58525DE4EF7DE20FL,     // 2:70
            0xB7BE3DCAB8E6CD83L, 0x19111DD07E64230CL,     // 2:72
            0x842359A03E2A367AL, 0x103F89F1F3401FB6L,     // 2:74
            0xDC710444D157D475L, 0xB835702334DA5845L,     // 2:76
            0x4320FC876511A6DCL, 0xD026ABC9D3679B8DL,     // 2:78
            0x17250EEE885C0B2BL, 0x90DAB52A387AE76FL,     // 2:7A
            0x31FED8D972C49C26L, 0x89CBA8FA461EC463L,     // 2:7C
            0x2FF5421677BCABB7L, 0x396F122F85E41D7DL,     // 2:7E
            0xA09B332430BAC6A8L, 0xC888E8CED7070560L,     // 2:80
            0xAEAF201AC682EE8FL, 0x1180D7268944A257L,     // 2:82
            0xF058A43628E7A5FCL, 0xBD4C4B8FBBCE2B07L,     // 2:84
            0xA1246DF34ABE7B49L, 0x7D5569B79BE9AF3CL,     // 2:86
            0xA9B5A705BD9EFA12L, 0xDB6B835BAA4BC0E8L,     // 2:88
            0x05793BAC8F147342L, 0x21C1512881848390L,     // 2:8A
            0xFDB0556C50D357E5L, 0x613D4FCB6A99FF72L,     // 2:8C
            0x03DCE2648E0CDA3EL, 0xE949B9E6568386F0L,     // 2:8E
            0xFC0F0BBB2AD7EA04L, 0x6A70675913B5A417L,     // 2:90
            0x7F36D5046FE1C8E3L, 0x0C57AF8D02304FF8L,     // 2:92
            0x32223ABDFCC84618L, 0x0891CAF6F720815BL,     // 2:94
            0xA63EEAEC31A26FD4L, 0x2507345374944D33L,     // 2:96
            0x49D28AC266394058L, 0xF5219F9AA7F3D6BEL,     // 2:98
            0x2D96FEA583B4CC68L, 0x5A31E1571B7585D0L,     // 2:9A
            0x8ED12FE53D02D0FEL, 0xDFADE6205F5B0E4BL,     // 2:9C
            0x4CABB16EE92D331AL, 0x04C6657BF510CEA3L,     // 2:9E
            0xD73C2CD6A87B8F10L, 0xE1D87310A1A307ABL,     // 2:A0
            0x6CD5BE9112AD0D6BL, 0x97C032354366F3F2L,     // 2:A2
            0xD4E0CEB22677552EL, 0x0000000000000000L,     // 2:A4
            0x29509BDE76A402CBL, 0xC27A9E8BD42FE3E4L,     // 2:A6
            0x5EF7842CEE654B73L, 0xAF107ECDBC86536EL,     // 2:A8
            0x3FCACBE784FCB401L, 0xD55F90655C73E8CFL,     // 2:AA
            0xE6C2F40FDABF1336L, 0xE8F6E7312C873B11L,     // 2:AC
            0xEB2A0555A28BE12FL, 0xE4A148BC2EB774E9L,     // 2:AE
            0x9B979DB84156BC0AL, 0x6EB60222E6A56AB4L,     // 2:B0
            0x87FFBBC4B026EC44L, 0xC703A5275B3B90A6L,     // 2:B2
            0x47E699FC9001687FL, 0x9C8D1AA73A4AA897L,     // 2:B4
            0x7CEA3760E1ED12DDL, 0x4EC80DDD1D2554C5L,     // 2:B6
            0x13E36B957D4CC588L, 0x5D2B66486069914DL,     // 2:B8
            0x92B90999CC7280B0L, 0x517CC9C56259DEB5L,     // 2:BA
            0xC937B619AD03B881L, 0xEC30824AD997F5B2L,     // 2:BC
            0xA45D565FC5AA080BL, 0xD6837201D27F32F1L,     // 2:BE
            0x635EF3789E9198ADL, 0x531F75769651B96AL,     // 2:C0
            0x4F77530A6721E924L, 0x486DD4151C3DFDB9L,     // 2:C2
            0x5F48DAFB9461F692L, 0x375B011173DC355AL,     // 2:C4
            0x3DA9775470F4D3DEL, 0x8D0DCD81B30E0AC0L,     // 2:C6
            0x36E45FC609D888BBL, 0x55BAACBE97491016L,     // 2:C8
            0x8CB29356C90AB721L, 0x76184125E2C5F459L,     // 2:CA
            0x99F4210BB55EDBD5L, 0x6F095CF59CA1D755L,     // 2:CC
            0x9F51F8C3B44672A9L, 0x3538BDA287D45285L,     // 2:CE
            0x50C39712185D6354L, 0xF23B1885DCEFC223L,     // 2:D0
            0x79930CCC6EF9619FL, 0xED8FDC9DA3934853L,     // 2:D2
            0xCB540AAA590BDF5EL, 0x5C94389F1A6D2CACL,     // 2:D4
            0xE77DAAD8A0BBAED7L, 0x28EFC5090CA0BF2AL,     // 2:D6
            0xBF2FF73C4FC64CD8L, 0xB37858B14DF60320L,     // 2:D8
            0xF8C96EC0DFC724A7L, 0x828680683F329F06L,     // 2:DA
            0x941CD051CD6A29CCL, 0xC3C5C05CAE2B5E05L,     // 2:DC
            0xB601631DC2E27062L, 0xC01922382027843BL,     // 2:DE
            0x24B86A840E90F0D2L, 0xD245177A276FFC52L,     // 2:E0
            0x0F8B4DE98C3C95C6L, 0x3E759530FEF809E0L,     // 2:E2
            0x0B4D2892792C5B65L, 0xC4DF4743D5374A98L,     // 2:E4
            0xA5E20888BFAEB5EAL, 0xBA56CC90C0D23F9AL,     // 2:E6
            0x38D04CF8FFE0A09CL, 0x62E1ADAFE495254CL,     // 2:E8
            0x0263BCB3F40867DFL, 0xCAEB547D230F62BFL,     // 2:EA
            0x6082111C109D4293L, 0xDAD4DD8CD04F7D09L,     // 2:EC
            0xEFEC602E579B2F8CL, 0x1FB4C4187F7C8A70L,     // 2:EE
            0xFFD3E9DFA4DB303AL, 0x7BF0B07F9AF10640L,     // 2:F0
            0xF49EC14DDDF76B5FL, 0x8F6E713247066D1FL,     // 2:F2
            0x339D646A86CCFBF9L, 0x64447467E58D8C30L,     // 2:F4
            0x2C29A072F9B07189L, 0xD8B7613F24471AD6L,     // 2:F6
            0x6627C8D41185EBEFL, 0xA347D140BEB61C96L,     // 2:F8
            0xDE12B8F7255FB3AAL, 0x9D324470404E1576L,     // 2:FA
            0x9306574EB6763D51L, 0xA80AF9D2C79A47F3L,     // 2:FC
            0x859C0777442E8B9BL, 0x69AC853D9DB97E29L      // 2:FE
            ,
            0x190A2C9B249DF23EL, 0x2F62F8B62263E1E9L,     // 3:00
            0x7A7F754740993655L, 0x330B7BA4D5564D9FL,     // 3:02
            0x4C17A16A46672582L, 0xB22F08EB7D05F5B8L,     // 3:04
            0x535F47F40BC148CCL, 0x3AEC5D27D4883037L,     // 3:06
            0x10ED0A1825438F96L, 0x516101F72C233D17L,     // 3:08
            0x13CC6F949FD04EAEL, 0x739853C441474BFDL,     // 3:0A
            0x653793D90D3F5B1BL, 0x5240647B96B0FC2FL,     // 3:0C
            0x0C84890AD27623E0L, 0xD7189B32703AAEA3L,     // 3:0E
            0x2685DE3523BD9C41L, 0x99317C5B11BFFEFAL,     // 3:10
            0x0D9BAA854F079703L, 0x70B93648FBD48AC5L,     // 3:12
            0xA80441FCE30BC6BEL, 0x7287704BDC36FF1EL,     // 3:14
            0xB65384ED33DC1F13L, 0xD36417343EE34408L,     // 3:16
            0x39CD38AB6E1BF10FL, 0x5AB861770A1F3564L,     // 3:18
            0x0EBACF09F594563BL, 0xD04572B884708530L,     // 3:1A
            0x3CAE9722BDB3AF47L, 0x4A556B6F2F5CBAF2L,     // 3:1C
            0xE1704F1F76C4BD74L, 0x5EC4ED7144C6DFCFL,     // 3:1E
            0x16AFC01D4C7810E6L, 0x283F113CD629CA7AL,     // 3:20
            0xAF59A8761741ED2DL, 0xEED5A3991E215FACL,     // 3:22
            0x3BF37EA849F984D4L, 0xE413E096A56CE33CL,     // 3:24
            0x2C439D3A98F020D1L, 0x637559DC6404C46BL,     // 3:26
            0x9E6C95D1E5F5D569L, 0x24BB9836045FE99AL,     // 3:28
            0x44EFA466DAC8ECC9L, 0xC6EAB2A5C80895D6L,     // 3:2A
            0x803B50C035220CC4L, 0x0321658CBA93C138L,     // 3:2C
            0x8F9EBC465DC7EE1CL, 0xD15A5137190131D3L,     // 3:2E
            0x0FA5EC8668E5E2D8L, 0x91C979578D1037B1L,     // 3:30
            0x0642CA05693B9F70L, 0xEFCA80168350EB4FL,     // 3:32
            0x38D21B24F36A45ECL, 0xBEAB81E1AF73D658L,     // 3:34
            0x8CBFD9CAE7542F24L, 0xFD19CC0D81F11102L,     // 3:36
            0x0AC6430FBB4DBC90L, 0x1D76A09D6A441895L,     // 3:38
            0x2A01573FF1CBBFA1L, 0xB572E161894FDE2BL,     // 3:3A
            0x8124734FA853B827L, 0x614B1FDF43E6B1B0L,     // 3:3C
            0x68AC395C4238CC18L, 0x21D837BFD7F7B7D2L,     // 3:3E
            0x20C714304A860331L, 0x5CFAAB726324AA14L,     // 3:40
            0x74C5BA4EB50D606EL, 0xF3A3030474654739L,     // 3:42
            0x23E671BCF015C209L, 0x45F087E947B9582AL,     // 3:44
            0xD8BD77B418DF4C7BL, 0xE06F6C90EBB50997L,     // 3:46
            0x0BD96080263C0873L, 0x7E03F9410E40DCFEL,     // 3:48
            0xB8E94BE4C6484928L, 0xFB5B0608E8CA8E72L,     // 3:4A
            0x1A2B49179E0E3306L, 0x4E29E76961855059L,     // 3:4C
            0x4F36C4E6FCF4E4BAL, 0x49740EE395CF7BCAL,     // 3:4E
            0xC2963EA386D17F7DL, 0x90D65AD810618352L,     // 3:50
            0x12D34C1B02A1FA4DL, 0xFA44258775BB3A91L,     // 3:52
            0x18150F14B9EC46DDL, 0x1491861E6B9A653DL,     // 3:54
            0x9A1019D7AB2C3FC2L, 0x3668D42D06FE13D7L,     // 3:56
            0xDCC1FBB25606A6D0L, 0x969490DD795A1C22L,     // 3:58
            0x3549B1A1BC6DD2EFL, 0xC94F5E23A0ED770EL,     // 3:5A
            0xB9F6686B5B39FDCBL, 0xC4D4F4A6EFEAE00DL,     // 3:5C
            0xE732851A1FFF2204L, 0x94AAD6DE5EB869F9L,     // 3:5E
            0x3F8FF2AE07206E7FL, 0xFE38A9813B62D03AL,     // 3:60
            0xA7A1AD7A8BEE2466L, 0x7B6056C8DDE882B6L,     // 3:62
            0x302A1E286FC58CA7L, 0x8DA0FA457A259BC7L,     // 3:64
            0xB3302B64E074415BL, 0x5402AE7EFF8B635FL,     // 3:66
            0x08F8050C9CAFC94BL, 0xAE468BF98A3059CEL,     // 3:68
            0x88C355CCA98DC58FL, 0xB10E6D67C7963480L,     // 3:6A
            0xBAD70DE7E1AA3CF3L, 0xBFB4A26E320262BBL,     // 3:6C
            0xCB711820870F02D5L, 0xCE12B7A954A75C9DL,     // 3:6E
            0x563CE87DD8691684L, 0x9F73B65E7884618AL,     // 3:70
            0x2B1E74B06CBA0B42L, 0x47CEC1EA605B2DF1L,     // 3:72
            0x1C698312F735AC76L, 0x5FDBCEFED9B76B2CL,     // 3:74
            0x831A354C8FB1CDFCL, 0x820516C312C0791FL,     // 3:76
            0xB74CA762AEADABF0L, 0xFC06EF821C80A5E1L,     // 3:78
            0x5723CBF24518A267L, 0x9D4DF05D5F661451L,     // 3:7A
            0x588627742DFD40BFL, 0xDA8331B73F3D39A0L,     // 3:7C
            0x17B0E392D109A405L, 0xF965400BCF28FBA9L,     // 3:7E
            0x7C3DBF4229A2A925L, 0x023E460327E275DBL,     // 3:80
            0x6CD0B55A0CE126B3L, 0xE62DA695828E96E7L,     // 3:82
            0x42AD6E63B3F373B9L, 0xE50CC319381D57DFL,     // 3:84
            0xC5CBD729729B54EEL, 0x46D1E265FD2A9912L,     // 3:86
            0x6428B056904EEFF8L, 0x8BE23040131E04B7L,     // 3:88
            0x6709D5DA2ADD2EC0L, 0x075DE98AF44A2B93L,     // 3:8A
            0x8447DCC67BFBE66FL, 0x6616F655B7AC9A23L,     // 3:8C
            0xD607B8BDED4B1A40L, 0x0563AF89D3A85E48L,     // 3:8E
            0x3DB1B4AD20C21BA4L, 0x11F22997B8323B75L,     // 3:90
            0x292032B34B587E99L, 0x7F1CDACE9331681DL,     // 3:92
            0x8E819FC9C0B65AFFL, 0xA1E3677FE2D5BB16L,     // 3:94
            0xCD33D225EE349DA5L, 0xD9A2543B85AEF898L,     // 3:96
            0x795E10CBFA0AF76DL, 0x25A4BBB9992E5D79L,     // 3:98
            0x78413344677B438EL, 0xF0826688CEF68601L,     // 3:9A
            0xD27B34BBA392F0EBL, 0x551D8DF162FAD7BCL,     // 3:9C
            0x1E57C511D0D7D9ADL, 0xDEFFBDB171E4D30BL,     // 3:9E
            0xF4FEEA8E802F6CAAL, 0xA480C8F6317DE55EL,     // 3:A0
            0xA0FC44F07FA40FF5L, 0x95B5F551C3C9DD1AL,     // 3:A2
            0x22F952336D6476EAL, 0x0000000000000000L,     // 3:A4
            0xA6BE8EF5169F9085L, 0xCC2CF1AA73452946L,     // 3:A6
            0x2E7DDB39BF12550AL, 0xD526DD3157D8DB78L,     // 3:A8
            0x486B2D6C08BECF29L, 0x9B0F3A58365D8B21L,     // 3:AA
            0xAC78CDFAADD22C15L, 0xBC95C7E28891A383L,     // 3:AC
            0x6A927F5F65DAB9C3L, 0xC3891D2C1BA0CB9EL,     // 3:AE
            0xEAA92F9F50F8B507L, 0xCF0D9426C9D6E87EL,     // 3:B0
            0xCA6E3BAF1A7EB636L, 0xAB25247059980786L,     // 3:B2
            0x69B31AD3DF4978FBL, 0xE2512A93CC577C4CL,     // 3:B4
            0xFF278A0EA61364D9L, 0x71A615C766A53E26L,     // 3:B6
            0x89DC764334FC716CL, 0xF87A638452594F4AL,     // 3:B8
            0xF2BC208BE914F3DAL, 0x8766B94AC1682757L,     // 3:BA
            0xBBC82E687CDB8810L, 0x626A7A53F9757088L,     // 3:BC
            0xA2C202F358467A2EL, 0x4D0882E5DB169161L,     // 3:BE
            0x09E7268301DE7DA8L, 0xE897699C771AC0DCL,     // 3:C0
            0xC8507DAC3D9CC3EDL, 0xC0A878A0A1330AA6L,     // 3:C2
            0x978BB352E42BA8C1L, 0xE9884A13EA6B743FL,     // 3:C4
            0x279AFDBABECC28A2L, 0x047C8C064ED9EAABL,     // 3:C6
            0x507E2278B15289F4L, 0x599904FBB08CF45CL,     // 3:C8
            0xBD8AE46D15E01760L, 0x31353DA7F2B43844L,     // 3:CA
            0x8558FF49E68A528CL, 0x76FBFC4D92EF15B5L,     // 3:CC
            0x3456922E211C660CL, 0x86799AC55C1993B4L,     // 3:CE
            0x3E90D1219A51DA9CL, 0x2D5CBEB505819432L,     // 3:D0
            0x982E5FD48CCE4A19L, 0xDB9C1238A24C8D43L,     // 3:D2
            0xD439FEBECAA96F9BL, 0x418C0BEF0960B281L,     // 3:D4
            0x158EA591F6EBD1DEL, 0x1F48E69E4DA66D4EL,     // 3:D6
            0x8AFD13CF8E6FB054L, 0xF5E1C9011D5ED849L,     // 3:D8
            0xE34E091C5126C8AFL, 0xAD67EE7530A398F6L,     // 3:DA
            0x43B24DEC2E82C75AL, 0x75DA99C1287CD48DL,     // 3:DC
            0x92E81CDB3783F689L, 0xA3DD217CC537CECDL,     // 3:DE
            0x60543C50DE970553L, 0x93F73F54AAF2426AL,     // 3:E0
            0xA91B62737E7A725DL, 0xF19D4507538732E2L,     // 3:E2
            0x77E4DFC20F9EA156L, 0x7D229CCDB4D31DC6L,     // 3:E4
            0x1B346A98037F87E5L, 0xEDF4C615A4B29E94L,     // 3:E6
            0x4093286094110662L, 0xB0114EE85AE78063L,     // 3:E8
            0x6FF1D0D6B672E78BL, 0x6DCF96D591909250L,     // 3:EA
            0xDFE09E3EEC9567E8L, 0x3214582B4827F97CL,     // 3:EC
            0xB46DC2EE143E6AC8L, 0xF6C0AC8DA7CD1971L,     // 3:EE
            0xEBB60C10CD8901E4L, 0xF7DF8F023ABCAD92L,     // 3:F0
            0x9C52D3D2C217A0B2L, 0x6B8D5CD0F8AB0D20L,     // 3:F2
            0x3777F7A29B8FA734L, 0x011F238F9D71B4E3L,     // 3:F4
            0xC1B75B2F3C42BE45L, 0x5DE588FDFE551EF7L,     // 3:F6
            0x6EEEF3592B035368L, 0xAA3A07FFC4E9B365L,     // 3:F8
            0xECEBE59A39C32A77L, 0x5BA742F8976E8187L,     // 3:FA
            0x4B4A48E0B22D0E11L, 0xDDDED83DCB771233L,     // 3:FC
            0xA59FEB79AC0C51BDL, 0xC7F5912A55792135L      // 3:FE
            ,
            0x20329B2CC87BBA05L, 0x4F5EB6F86546A531L,     // 4:00
            0xD4F44775F751B6B1L, 0x8266A47B850DFA8BL,     // 4:02
            0xBB986AA15A6CA985L, 0xC979EB08F9AE0F99L,     // 4:04
            0x2DA6F447A2375EA1L, 0x1E74275DCD7D8576L,     // 4:06
            0xBC20180A800BC5F8L, 0xB4A2F701B2DC65BEL,     // 4:08
            0xE726946F981B6D66L, 0x48E6C453BF21C94CL,     // 4:0A
            0x42CAD9930F0A4195L, 0xEFA47B64AACCCD20L,     // 4:0C
            0x71180A8960409A42L, 0x8BB3329BF6A44E0CL,     // 4:0E
            0xD34C35DE2D36DACCL, 0xA92F5B7CBC23DC96L,     // 4:10
            0xB31A85AA68BB09C3L, 0x13E04836A73161D2L,     // 4:12
            0xB24DFC4129C51D02L, 0x8AE44B70B7DA5ACDL,     // 4:14
            0xE671ED84D96579A7L, 0xA4BB3417D66F3832L,     // 4:16
            0x4572AB38D56D2DE8L, 0xB1B47761EA47215CL,     // 4:18
            0xE81C09CF70ABA15DL, 0xFFBDB872CE7F90ACL,     // 4:1A
            0xA8782297FD5DC857L, 0x0D946F6B6A4CE4A4L,     // 4:1C
            0xE4DF1F4F5B995138L, 0x9EBC71EDCA8C5762L,     // 4:1E
            0x0A2C1DC0B02B88D9L, 0x3B503C115D9D7B91L,     // 4:20
            0xC64376A8111EC3A2L, 0xCEC199A323C963E4L,     // 4:22
            0xDC76A87EC58616F7L, 0x09D596E073A9B487L,     // 4:24
            0x14583A9D7D560DAFL, 0xF4C6DC593F2A0CB4L,     // 4:26
            0xDD21D19584F80236L, 0x4A4836983DDDE1D3L,     // 4:28
            0xE58866A41AE745F9L, 0xF591A5B27E541875L,     // 4:2A
            0x891DC05074586693L, 0x5B068C651810A89EL,     // 4:2C
            0xA30346BC0C08544FL, 0x3DBF3751C684032DL,     // 4:2E
            0x2A1E86EC785032DCL, 0xF73F5779FCA830EAL,     // 4:30
            0xB60C05CA30204D21L, 0x0CC316802B32F065L,     // 4:32
            0x8770241BDD96BE69L, 0xB861E18199EE95DBL,     // 4:34
            0xF805CAD91418FCD1L, 0x29E70DCCBBD20E82L,     // 4:36
            0xC7140F435060D763L, 0x0F3A9DA0E8B0CC3BL,     // 4:38
            0xA2543F574D76408EL, 0xBD7761E1C175D139L,     // 4:3A
            0x4B1F4F737CA3F512L, 0x6DC2DF1F2FC137ABL,     // 4:3C
            0xF1D05C3967B14856L, 0xA742BF3715ED046CL,     // 4:3E
            0x654030141D1697EDL, 0x07B872ABDA676C7DL,     // 4:40
            0x3CE84EBA87FA17ECL, 0xC1FB0403CB79AFDFL,     // 4:42
            0x3E46BC7105063F73L, 0x278AE987121CD678L,     // 4:44
            0xA1ADB4778EF47CD0L, 0x26DD906C5362C2B9L,     // 4:46
            0x05168060589B44E2L, 0xFBFC41F9D79AC08FL,     // 4:48
            0x0E6DE44BA9CED8FAL, 0x9FEB08068BF243A3L,     // 4:4A
            0x7B341749D06B129BL, 0x229C69E74A87929AL,     // 4:4C
            0xE09EE6C4427C011BL, 0x5692E30E725C4C3AL,     // 4:4E
            0xDA99A33E5E9F6E4BL, 0x353DD85AF453A36BL,     // 4:50
            0x25241B4C90E0FEE7L, 0x5DE987258309D022L,     // 4:52
            0xE230140FC0802984L, 0x93281E86A0C0B3C6L,     // 4:54
            0xF229D719A4337408L, 0x6F6C2DD4AD3D1F34L,     // 4:56
            0x8EA5B2FBAE3F0AEEL, 0x8331DD90C473EE4AL,     // 4:58
            0x346AA1B1B52DB7AAL, 0xDF8F235E06042AA9L,     // 4:5A
            0xCC6F6B68A1354B7BL, 0x6C95A6F46EBF236AL,     // 4:5C
            0x52D31A856BB91C19L, 0x1A35DED6D498D555L,     // 4:5E
            0xF37EAEF2E54D60C9L, 0x72E181A9A3C2A61CL,     // 4:60
            0x98537AAD51952FDEL, 0x16F6C856FFAA2530L,     // 4:62
            0xD960281E9D1D5215L, 0x3A0745FA1CE36F50L,     // 4:64
            0x0B7B642BF1559C18L, 0x59A87EAE9AEC8001L,     // 4:66
            0x5E100C05408BEC7CL, 0x0441F98B19E55023L,     // 4:68
            0xD70DCC5534D38AEFL, 0x927F676DE1BEA707L,     // 4:6A
            0x9769E70DB925E3E5L, 0x7A636EA29115065AL,     // 4:6C
            0x468B201816EF11B6L, 0xAB81A9B73EDFF409L,     // 4:6E
            0xC0AC7DE88A07BB1EL, 0x1F235EB68C0391B7L,     // 4:70
            0x6056B074458DD30FL, 0xBE8EEAC102F7ED67L,     // 4:72
            0xCD381283E04B5FBAL, 0x5CBEFECEC277C4E3L,     // 4:74
            0xD21B4C356C48CE0DL, 0x1019C31664B35D8CL,     // 4:76
            0x247362A7D19EEA26L, 0xEBE582EFB3299D03L,     // 4:78
            0x02AEF2CB82FC289FL, 0x86275DF09CE8AAA8L,     // 4:7A
            0x28B07427FAAC1A43L, 0x38A9B7319E1F47CFL,     // 4:7C
            0xC82E92E3B8D01B58L, 0x06EF0B409B1978BCL,     // 4:7E
            0x62F842BFC771FB90L, 0x9904034610EB3B1FL,     // 4:80
            0xDED85AB5477A3E68L, 0x90D195A663428F98L,     // 4:82
            0x5384636E2AC708D8L, 0xCBD719C37B522706L,     // 4:84
            0xAE9729D76644B0EBL, 0x7C8C65E20A0C7EE6L,     // 4:86
            0x80C856B007F1D214L, 0x8C0B40302CC32271L,     // 4:88
            0xDBCEDAD51FE17A8AL, 0x740E8AE938DBDEA0L,     // 4:8A
            0xA615C6DC549310ADL, 0x19CC55F6171AE90BL,     // 4:8C
            0x49B1BDB8FE5FDD8DL, 0xED0A89AF2830E5BFL,     // 4:8E
            0x6A7AADB4F5A65BD6L, 0x7E22972988F05679L,     // 4:90
            0xF952B3325566E810L, 0x39FECEDADF61530EL,     // 4:92
            0x6101C99F04F3C7CEL, 0x2E5F7F6761B562FFL,     // 4:94
            0xF08725D226CF5C97L, 0x63AF3B54860FEF51L,     // 4:96
            0x8FF2CB10EF411E2FL, 0x884AB9BB35267252L,     // 4:98
            0x4DF04433E7BA8DAEL, 0x9AFD8866D3690741L,     // 4:9A
            0x66B9BB34DE94ABB3L, 0x9BAAF18D92171380L,     // 4:9C
            0x543C11C5F0A064A5L, 0x17A1B1BDBED431F1L,     // 4:9E
            0xB5F58EEAF3A2717FL, 0xC355F6C849858740L,     // 4:A0
            0xEC5DF044694EF17EL, 0xD83751F5DC6346D4L,     // 4:A2
            0xFC4433520DFDACF2L, 0x0000000000000000L,     // 4:A4
            0x5A51F58E596EBC5FL, 0x3285AAF12E34CF16L,     // 4:A6
            0x8D5C39DB6DBD36B0L, 0x12B731DDE64F7513L,     // 4:A8
            0x94906C2D7AA7DFBBL, 0x302B583AACC8E789L,     // 4:AA
            0x9D45FACD090E6B3CL, 0x2165E2C78905AEC4L,     // 4:AC
            0x68D45F7F775A7349L, 0x189B2C1D5664FDCAL,     // 4:AE
            0xE1C99F2F030215DAL, 0x6983269436246788L,     // 4:B0
            0x8489AF3B1E148237L, 0xE94B702431D5B59CL,     // 4:B2
            0x33D2D31A6F4ADBD7L, 0xBFD9932A4389F9A6L,     // 4:B4
            0xB0E30E8AAB39359DL, 0xD1E2C715AFCAF253L,     // 4:B6
            0x150F43763C28196EL, 0xC4ED846393E2EB3DL,     // 4:B8
            0x03F98B20C3823C5EL, 0xFD134AB94C83B833L,     // 4:BA
            0x556B682EB1DE7064L, 0x36C4537A37D19F35L,     // 4:BC
            0x7559F30279A5CA61L, 0x799AE58252973A04L,     // 4:BE
            0x9C12832648707FFDL, 0x78CD9C6913E92EC5L,     // 4:C0
            0x1D8DAC7D0EFFB928L, 0x439DA0784E745554L,     // 4:C2
            0x413352B3CC887DCBL, 0xBACF134A1B12BD44L,     // 4:C4
            0x114EBAFD25CD494DL, 0x2F08068C20CB763EL,     // 4:C6
            0x76A07822BA27F63FL, 0xEAB2FB04F25789C2L,     // 4:C8
            0xE3676DE481FE3D45L, 0x1B62A73D95E6C194L,     // 4:CA
            0x641749FF5C68832CL, 0xA5EC4DFC97112CF3L,     // 4:CC
            0xF6682E92BDD6242BL, 0x3F11C59A44782BB2L,     // 4:CE
            0x317C21D1EDB6F348L, 0xD65AB5BE75AD9E2EL,     // 4:D0
            0x6B2DD45FB4D84F17L, 0xFAAB381296E4D44EL,     // 4:D2
            0xD0B5BEFEEEB4E692L, 0x0882EF0B32D7A046L,     // 4:D4
            0x512A91A5A83B2047L, 0x963E9EE6F85BF724L,     // 4:D6
            0x4E09CF132438B1F0L, 0x77F701C9FB59E2FEL,     // 4:D8
            0x7DDB1C094B726A27L, 0x5F4775EE01F5F8BDL,     // 4:DA
            0x9186EC4D223C9B59L, 0xFEEAC1998F01846DL,     // 4:DC
            0xAC39DB1CE4B89874L, 0xB75B7C21715E59E0L,     // 4:DE
            0xAFC0503C273AA42AL, 0x6E3B543FEC430BF5L,     // 4:E0
            0x704F7362213E8E83L, 0x58FF0745DB9294C0L,     // 4:E2
            0x67EEC2DF9FEABF72L, 0xA0FACD9CCF8A6811L,     // 4:E4
            0xB936986AD890811AL, 0x95C715C63BD9CB7AL,     // 4:E6
            0xCA8060283A2C33C7L, 0x507DE84EE9453486L,     // 4:E8
            0x85DED6D05F6A96F6L, 0x1CDAD5964F81ADE9L,     // 4:EA
            0xD5A33E9EB62FA270L, 0x40642B588DF6690AL,     // 4:EC
            0x7F75EEC2C98E42B8L, 0x2CF18DACE3494A60L,     // 4:EE
            0x23CB100C0BF9865BL, 0xEEF3028FEBB2D9E1L,     // 4:F0
            0x4425D2D394133929L, 0xAAD6D05C7FA1E0C8L,     // 4:F2
            0xAD6EA2F7A5C68CB5L, 0xC2028F2308FB9381L,     // 4:F4
            0x819F2F5B468FC6D5L, 0xC5BAFD88D29CFFFCL,     // 4:F6
            0x47DC59F357910577L, 0x2B49FF07392E261DL,     // 4:F8
            0x57C59AE5332258FBL, 0x73B6F842E2BCB2DDL,     // 4:FA
            0xCF96E04862B77725L, 0x4CA73DD8A6C4996FL,     // 4:FC
            0x015779EB417E14C1L, 0x37932A9176AF8BF4L      // 4:FE
            ,
            0xCCE4CD3AA968B245L, 0x089D5484E80B7FAFL,     // 5:00
            0x638246C1B3548304L, 0xD2FE0EC8C2355492L,     // 5:02
            0xA7FBDF7FF2374EEEL, 0x4DF1600C92337A16L,     // 5:04
            0x84E503EA523B12FBL, 0x0790BBFD53AB0C4AL,     // 5:06
            0x198A780F38F6EA9DL, 0x2AB30C8F55EC48CBL,     // 5:08
            0xE0F7FED6B2C49DB5L, 0xB6ECF3F422CADBDCL,     // 5:0A
            0x409C9A541358DF11L, 0xD3CE8A56DFDE3FE3L,     // 5:0C
            0xC3E9224312C8C1A0L, 0x0D6DFA58816BA507L,     // 5:0E
            0xDDF3E1B179952777L, 0x04C02A42748BB1D9L,     // 5:10
            0x94C2ABFF9F2DECB8L, 0x4F91752DA8F8ACF4L,     // 5:12
            0x78682BEFB169BF7BL, 0xE1C77A48AF2FF6C4L,     // 5:14
            0x0C5D7EC69C80CE76L, 0x4CC1E4928FD81167L,     // 5:16
            0xFEED3D24D9997B62L, 0x518BB6DFC3A54A23L,     // 5:18
            0x6DBF2D26151F9B90L, 0xB5BC624B05EA664FL,     // 5:1A
            0xE86AAA525ACFE21AL, 0x4801CED0FB53A0BEL,     // 5:1C
            0xC91463E6C00868EDL, 0x1027A815CD16FE43L,     // 5:1E
            0xF67069A0319204CDL, 0xB04CCC976C8ABCE7L,     // 5:20
            0xC0B9B3FC35E87C33L, 0xF380C77C58F2DE65L,     // 5:22
            0x50BB3241DE4E2152L, 0xDF93F490435EF195L,     // 5:24
            0xF1E0D25D62390887L, 0xAF668BFB1A3C3141L,     // 5:26
            0xBC11B251F00A7291L, 0x73A5EED47E427D47L,     // 5:28
            0x25BEE3F6EE4C3B2EL, 0x43CC0BEB34786282L,     // 5:2A
            0xC824E778DDE3039CL, 0xF97D86D98A327728L,     // 5:2C
            0xF2B043E24519B514L, 0xE297EBF7880F4B57L,     // 5:2E
            0x3A94A49A98FAB688L, 0x868516CB68F0C419L,     // 5:30
            0xEFFA11AF0964EE50L, 0xA4AB4EC0D517F37DL,     // 5:32
            0xA9C6B498547C567AL, 0x8E18424F80FBBBB6L,     // 5:34
            0x0BCDC53BCF2BC23CL, 0x137739AAEA3643D0L,     // 5:36
            0x2C1333EC1BAC2FF0L, 0x8D48D3F0A7DB0625L,     // 5:38
            0x1E1AC3F26B5DE6D7L, 0xF520F81F16B2B95EL,     // 5:3A
            0x9F0F6EC450062E84L, 0x0130849E1DEB6B71L,     // 5:3C
            0xD45E31AB8C7533A9L, 0x652279A2FD14E43FL,     // 5:3E
            0x3209F01E70F1C927L, 0xBE71A770CAC1A473L,     // 5:40
            0x0E3D6BE7A64B1894L, 0x7EC8148CFF29D840L,     // 5:42
            0xCB7476C7FAC3BE0FL, 0x72956A4A63A91636L,     // 5:44
            0x37F95EC21991138FL, 0x9E3FEA5A4DED45F5L,     // 5:46
            0x7B38BA50964902E8L, 0x222E580BBDE73764L,     // 5:48
            0x61E253E0899F55E6L, 0xFC8D2805E352AD80L,     // 5:4A
            0x35994BE3235AC56DL, 0x09ADD01AF5E014DEL,     // 5:4C
            0x5E8659A6780539C6L, 0xB17C48097161D796L,     // 5:4E
            0x026015213ACBD6E2L, 0xD1AE9F77E515E901L,     // 5:50
            0xB7DC776A3F21B0ADL, 0xABA6A1B96EB78098L,     // 5:52
            0x9BCF4486248D9F5DL, 0x582666C536455EFDL,     // 5:54
            0xFDBDAC9BFEB9C6F1L, 0xC47999BE4163CDEAL,     // 5:56
            0x765540081722A7EFL, 0x3E548ED8EC710751L,     // 5:58
            0x3D041F67CB51BAC2L, 0x7958AF71AC82D40AL,     // 5:5A
            0x36C9DA5C047A78FEL, 0xED9A048E33AF38B2L,     // 5:5C
            0x26EE7249C96C86BDL, 0x900281BDEBA65D61L,     // 5:5E
            0x11172C8BD0FD9532L, 0xEA0ABF73600434F8L,     // 5:60
            0x42FC8F75299309F3L, 0x34A9CF7D3EB1AE1CL,     // 5:62
            0x2B838811480723BAL, 0x5CE64C8742CEEF24L,     // 5:64
            0x1ADAE9B01FD6570EL, 0x3C349BF9D6BAD1B3L,     // 5:66
            0x82453C891C7B75C0L, 0x97923A40B80D512BL,     // 5:68
            0x4A61DBF1C198765CL, 0xB48CE6D518010D3EL,     // 5:6A
            0xCFB45C858E480FD6L, 0xD933CBF30D1E96AEL,     // 5:6C
            0xD70EA014AB558E3AL, 0xC189376228031742L,     // 5:6E
            0x9262949CD16D8B83L, 0xEB3A3BED7DEF5F89L,     // 5:70
            0x49314A4EE6B8CBCFL, 0xDCC3652F647E4C06L,     // 5:72
            0xDA635A4C2A3E2B3DL, 0x470C21A940F3D35BL,     // 5:74
            0x315961A157D174B4L, 0x6672E81DDA3459ACL,     // 5:76
            0x5B76F77A1165E36EL, 0x445CB01667D36EC8L,     // 5:78
            0xC5491D205C88A69BL, 0x456C34887A3805B9L,     // 5:7A
            0xFFDDB9BAC4721013L, 0x99AF51A71E4649BFL,     // 5:7C
            0xA15BE01CBC7729D5L, 0x52DB2760E485F7B0L,     // 5:7E
            0x8C78576EBA306D54L, 0xAE560F6507D75A30L,     // 5:80
            0x95F22F6182C687C9L, 0x71C5FBF54489ABA5L,     // 5:82
            0xCA44F259E728D57EL, 0x88B87D2CCEBBDC8DL,     // 5:84
            0xBAB18D32BE4A15AAL, 0x8BE8EC93E99B611EL,     // 5:86
            0x17B713E89EBDF209L, 0xB31C5D284BAA0174L,     // 5:88
            0xEECA9531148F8521L, 0xB8D198138481C348L,     // 5:8A
            0x8988F9B2D350B7FCL, 0xB9E11C8D996AA839L,     // 5:8C
            0x5A4673E40C8E881FL, 0x1687977683569978L,     // 5:8E
            0xBF4123EED72ACF02L, 0x4EA1F1B3B513C785L,     // 5:90
            0xE767452BE16F91FFL, 0x7505D1B730021A7CL,     // 5:92
            0xA59BCA5EC8FC980CL, 0xAD069EDA20F7E7A3L,     // 5:94
            0x38F4B1BBA231606AL, 0x60D2D77E94743E97L,     // 5:96
            0x9AFFC0183966F42CL, 0x248E6768F3A7505FL,     // 5:98
            0xCDD449A4B483D934L, 0x87B59255751BAF68L,     // 5:9A
            0x1BEA6D2E023D3C7FL, 0x6B1F12455B5FFCABL,     // 5:9C
            0x743555292DE9710DL, 0xD8034F6D10F5FDDFL,     // 5:9E
            0xC6198C9F7BA81B08L, 0xBB8109ACA3A17EDBL,     // 5:A0
            0xFA2D1766AD12CABBL, 0xC729080166437079L,     // 5:A2
            0x9C5FFF7B77269317L, 0x0000000000000000L,     // 5:A4
            0x15D706C9A47624EBL, 0x6FDF38072FD44D72L,     // 5:A6
            0x5FB6DD3865EE52B7L, 0xA33BF53D86BCFF37L,     // 5:A8
            0xE657C1B5FC84FA8EL, 0xAA962527735CEBE9L,     // 5:AA
            0x39C43525BFDA0B1BL, 0x204E4D2A872CE186L,     // 5:AC
            0x7A083ECE8BA26999L, 0x554B9C9DB72EFBFAL,     // 5:AE
            0xB22CD9B656416A05L, 0x96A2BEDEA5E63A5AL,     // 5:B0
            0x802529A826B0A322L, 0x8115AD363B5BC853L,     // 5:B2
            0x8375B81701901EB1L, 0x3069E53F4A3A1FC5L,     // 5:B4
            0xBD2136CFEDE119E0L, 0x18BAFC91251D81ECL,     // 5:B6
            0x1D4A524D4C7D5B44L, 0x05F0AEDC6960DAA8L,     // 5:B8
            0x29E39D3072CCF558L, 0x70F57F6B5962C0D4L,     // 5:BA
            0x989FD53903AD22CEL, 0xF84D024797D91C59L,     // 5:BC
            0x547B1803AAC5908BL, 0xF0D056C37FD263F6L,     // 5:BE
            0xD56EB535919E58D8L, 0x1C7AD6D351963035L,     // 5:C0
            0x2E7326CD2167F912L, 0xAC361A443D1C8CD2L,     // 5:C2
            0x697F076461942A49L, 0x4B515F6FDC731D2DL,     // 5:C4
            0x8AD8680DF4700A6FL, 0x41AC1ECA0EB3B460L,     // 5:C6
            0x7D988533D80965D3L, 0xA8F6300649973D0BL,     // 5:C8
            0x7765C4960AC9CC9EL, 0x7CA801ADC5E20EA2L,     // 5:CA
            0xDEA3700E5EB59AE4L, 0xA06B6482A19C42A4L,     // 5:CC
            0x6A2F96DB46B497DAL, 0x27DEF6D7D487EDCCL,     // 5:CE
            0x463CA5375D18B82AL, 0xA6CB5BE1EFDC259FL,     // 5:D0
            0x53EBA3FEF96E9CC1L, 0xCE84D81B93A364A7L,     // 5:D2
            0xF4107C810B59D22FL, 0x333974806D1AA256L,     // 5:D4
            0x0F0DEF79BBA073E5L, 0x231EDC95A00C5C15L,     // 5:D6
            0xE437D494C64F2C6CL, 0x91320523F64D3610L,     // 5:D8
            0x67426C83C7DF32DDL, 0x6EEFBC99323F2603L,     // 5:DA
            0x9D6F7BE56ACDF866L, 0x5916E25B2BAE358CL,     // 5:DC
            0x7FF89012E2C2B331L, 0x035091BF2720BD93L,     // 5:DE
            0x561B0D22900E4669L, 0x28D319AE6F279E29L,     // 5:E0
            0x2F43A2533C8C9263L, 0xD09E1BE9F8FE8270L,     // 5:E2
            0xF740ED3E2C796FBCL, 0xDB53DED237D5404CL,     // 5:E4
            0x62B2C25FAEBFE875L, 0x0AFD41A5D2C0A94DL,     // 5:E6
            0x6412FD3CE0FF8F4EL, 0xE3A76F6995E42026L,     // 5:E8
            0x6C8FA9B808F4F0E1L, 0xC2D9A6DD0F23AAD1L,     // 5:EA
            0x8F28C6D19D10D0C7L, 0x85D587744FD0798AL,     // 5:EC
            0xA20B71A39B579446L, 0x684F83FA7C7F4138L,     // 5:EE
            0xE507500ADBA4471DL, 0x3F640A46F19A6C20L,     // 5:F0
            0x1247BD34F7DD28A1L, 0x2D23B77206474481L,     // 5:F2
            0x93521002CC86E0F2L, 0x572B89BC8DE52D18L,     // 5:F4
            0xFB1D93F8B0F9A1CAL, 0xE95A2ECC4724896BL,     // 5:F6
            0x3BA420048511DDF9L, 0xD63E248AB6BEE54BL,     // 5:F8
            0x5DD6C8195F258455L, 0x06A03F634E40673BL,     // 5:FA
            0x1F2A476C76B68DA6L, 0x217EC9B49AC78AF7L,     // 5:FC
            0xECAA80102E4453C3L, 0x14E78257B99D4F9AL      // 5:FE}
            ,
            0xDE553F8C05A811C8L, 0x1906B59631B4F565L,     // 6:00
            0x436E70D6B1964FF7L, 0x36D343CB8B1E9D85L,     // 6:02
            0x843DFACC858AAB5AL, 0xFDFC95C299BFC7F9L,     // 6:04
            0x0F634BDEA1D51FA2L, 0x6D458B3B76EFB3CDL,     // 6:06
            0x85C3F77CF8593F80L, 0x3C91315FBE737CB2L,     // 6:08
            0x2148B03366ACE398L, 0x18F8B8264C6761BFL,     // 6:0A
            0xC830C1C495C9FB0FL, 0x981A76102086A0AAL,     // 6:0C
            0xAA16012142F35760L, 0x35CC54060C763CF6L,     // 6:0E
            0x42907D66CC45DB2DL, 0x8203D44B965AF4BCL,     // 6:10
            0x3D6F3CEFC3A0E868L, 0xBC73FF69D292BDA7L,     // 6:12
            0x8722ED0102E20A29L, 0x8F8185E8CD34DEB7L,     // 6:14
            0x9B0561DDA7EE01D9L, 0x5335A0193227FAD6L,     // 6:16
            0xC9CECC74E81A6FD5L, 0x54F5832E5C2431EAL,     // 6:18
            0x99E47BA05D553470L, 0xF7BEE756ACD226CEL,     // 6:1A
            0x384E05A5571816FDL, 0xD1367452A47D0E6AL,     // 6:1C
            0xF29FDE1C386AD85BL, 0x320C77316275F7CAL,     // 6:1E
            0xD0C879E2D9AE9AB0L, 0xDB7406C69110EF5DL,     // 6:20
            0x45505E51A2461011L, 0xFC029872E46C5323L,     // 6:22
            0xFA3CB6F5F7BC0CC5L, 0x031F17CD8768A173L,     // 6:24
            0xBD8DF2D9AF41297DL, 0x9D3B4F5AB43E5E3FL,     // 6:26
            0x4071671B36FEEE84L, 0x716207E7D3E3B83DL,     // 6:28
            0x48D20FF2F9283A1AL, 0x27769EB4757CBC7EL,     // 6:2A
            0x5C56EBC793F2E574L, 0xA48B474F9EF5DC18L,     // 6:2C
            0x52CBADA94FF46E0CL, 0x60C7DA982D8199C6L,     // 6:2E
            0x0E9D466EDC068B78L, 0x4EEC2175EAF865FCL,     // 6:30
            0x550B8E9E21F7A530L, 0x6B7BA5BC653FEC2BL,     // 6:32
            0x5EB7F1BA6949D0DDL, 0x57EA94E3DB4C9099L,     // 6:34
            0xF640EAE6D101B214L, 0xDD4A284182C0B0BBL,     // 6:36
            0xFF1D8FBF6304F250L, 0xB8ACCB933BF9D7E8L,     // 6:38
            0xE8867C478EB68C4DL, 0x3F8E2692391BDDC1L,     // 6:3A
            0xCB2FD60912A15A7CL, 0xAEC935DBAB983D2FL,     // 6:3C
            0xF55FFD2B56691367L, 0x80E2CE366CE1C115L,     // 6:3E
            0x179BF3F8EDB27E1DL, 0x01FE0DB07DD394DAL,     // 6:40
            0xDA8A0B76ECC37B87L, 0x44AE53E1DF9584CBL,     // 6:42
            0xB310B4B77347A205L, 0xDFAB323C787B8512L,     // 6:44
            0x3B511268D070B78EL, 0x65E6E3D2B9396753L,     // 6:46
            0x6864B271E2574D58L, 0x259784C98FC789D7L,     // 6:48
            0x02E11A7DFABB35A9L, 0x8841A6DFA337158BL,     // 6:4A
            0x7ADE78C39B5DCDD0L, 0xB7CF804D9A2CC84AL,     // 6:4C
            0x20B6BD831B7F7742L, 0x75BD331D3A88D272L,     // 6:4E
            0x418F6AAB4B2D7A5EL, 0xD9951CBB6BABDAF4L,     // 6:50
            0xB6318DFDE7FF5C90L, 0x1F389B112264AA83L,     // 6:52
            0x492C024284FBAEC0L, 0xE33A0363C608F9A0L,     // 6:54
            0x2688930408AF28A4L, 0xC7538A1A341CE4ADL,     // 6:56
            0x5DA8E677EE2171AEL, 0x8C9E92254A5C7FC4L,     // 6:58
            0x63D8CD55AAE938B5L, 0x29EBD8DAA97A3706L,     // 6:5A
            0x959827B37BE88AA1L, 0x1484E4356ADADF6EL,     // 6:5C
            0xA7945082199D7D6BL, 0xBF6CE8A455FA1CD4L,     // 6:5E
            0x9CC542EAC9EDCAE5L, 0x79C16F0E1C356CA3L,     // 6:60
            0x89BFAB6FDEE48151L, 0xD4174D1830C5F0FFL,     // 6:62
            0x9258048415EB419DL, 0x6139D72850520D1CL,     // 6:64
            0x6A85A80C18EC78F1L, 0xCD11F88E0171059AL,     // 6:66
            0xCCEFF53E7CA29140L, 0xD229639F2315AF19L,     // 6:68
            0x90B91EF9EF507434L, 0x5977D28D074A1BE1L,     // 6:6A
            0x311360FCE51D56B9L, 0xC093A92D5A1F2F91L,     // 6:6C
            0x1A19A25BB6DC5416L, 0xEB996B8A09DE2D3EL,     // 6:6E
            0xFEE3820F1ED7668AL, 0xD7085AD5B7AD518CL,     // 6:70
            0x7FFF41890FE53345L, 0xEC5948BD67DDE602L,     // 6:72
            0x2FD5F65DBAAA68E0L, 0xA5754AFFE32648C2L,     // 6:74
            0xF8DDAC880D07396CL, 0x6FA491468C548664L,     // 6:76
            0x0C7C5C1326BDBED1L, 0x4A33158F03930FB3L,     // 6:78
            0x699ABFC19F84D982L, 0xE4FA2054A80B329CL,     // 6:7A
            0x6707F9AF438252FAL, 0x08A368E9CFD6D49EL,     // 6:7C
            0x47B1442C58FD25B8L, 0xBBB3DC5EBC91769BL,     // 6:7E
            0x1665FE489061EAC7L, 0x33F27A811FA66310L,     // 6:80
            0x93A609346838D547L, 0x30ED6D4C98CEC263L,     // 6:82
            0x1DD9816CD8DF9F2AL, 0x94662A03063B1E7BL,     // 6:84
            0x83FDD9FBEB896066L, 0x7B207573E68E590AL,     // 6:86
            0x5F49FC0A149A4407L, 0x343259B671A5A82CL,     // 6:88
            0xFBC2BB458A6F981FL, 0xC272B350A0A41A38L,     // 6:8A
            0x3AAF1FD8ADA32354L, 0x6CBB868B0B3C2717L,     // 6:8C
            0xA2B569C88D2583FEL, 0xF180C9D1BF027928L,     // 6:8E
            0xAF37386BD64BA9F5L, 0x12BACAB2790A8088L,     // 6:90
            0x4C0D3B0810435055L, 0xB2EEB9070E9436DFL,     // 6:92
            0xC5B29067CEA7D104L, 0xDCB425F1FF132461L,     // 6:94
            0x4F122CC5972BF126L, 0xAC282FA651230886L,     // 6:96
            0xE7E537992F6393EFL, 0xE61B3A2952B00735L,     // 6:98
            0x709C0A57AE302CE7L, 0xE02514AE416058D3L,     // 6:9A
            0xC44C9DD7B37445DEL, 0x5A68C5408022BA92L,     // 6:9C
            0x1C278CDCA50C0BF0L, 0x6E5A9CF6F18712BEL,     // 6:9E
            0x86DCE0B17F319EF3L, 0x2D34EC2040115D49L,     // 6:A0
            0x4BCD183F7E409B69L, 0x2815D56AD4A9A3DCL,     // 6:A2
            0x24698979F2141D0DL, 0x0000000000000000L,     // 6:A4
            0x1EC696A15FB73E59L, 0xD86B110B16784E2EL,     // 6:A6
            0x8E7F8858B0E74A6DL, 0x063E2E8713D05FE6L,     // 6:A8
            0xE2C40ED3BBDB6D7AL, 0xB1F1AECA89FC97ACL,     // 6:AA
            0xE1DB191E3CB3CC09L, 0x6418EE62C4EAF389L,     // 6:AC
            0xC6AD87AA49CF7077L, 0xD6F65765CA7EC556L,     // 6:AE
            0x9AFB6C6DDA3D9503L, 0x7CE05644888D9236L,     // 6:B0
            0x8D609F95378FEB1EL, 0x23A9AA4E9C17D631L,     // 6:B2
            0x6226C0E5D73AAC6FL, 0x56149953A69F0443L,     // 6:B4
            0xEEB852C09D66D3ABL, 0x2B0AC2A753C102AFL,     // 6:B6
            0x07C023376E03CB3CL, 0x2CCAE1903DC2C993L,     // 6:B8
            0xD3D76E2F5EC63BC3L, 0x9E2458973356FF4CL,     // 6:BA
            0xA66A5D32644EE9B1L, 0x0A427294356DE137L,     // 6:BC
            0x783F62BE61E6F879L, 0x1344C70204D91452L,     // 6:BE
            0x5B96C8F0FDF12E48L, 0xA90916ECC59BF613L,     // 6:C0
            0xBE92E5142829880EL, 0x727D102A548B194EL,     // 6:C2
            0x1BE7AFEBCB0FC0CCL, 0x3E702B2244C8491BL,     // 6:C4
            0xD5E940A84D166425L, 0x66F9F41F3E51C620L,     // 6:C6
            0xABE80C913F20C3BAL, 0xF07EC461C2D1EDF2L,     // 6:C8
            0xF361D3AC45B94C81L, 0x0521394A94B8FE95L,     // 6:CA
            0xADD622162CF09C5CL, 0xE97871F7F3651897L,     // 6:CC
            0xF4A1F09B2BBA87BDL, 0x095D6559B2054044L,     // 6:CE
            0x0BBC7F2448BE75EDL, 0x2AF4CF172E129675L,     // 6:D0
            0x157AE98517094BB4L, 0x9FDA55274E856B96L,     // 6:D2
            0x914713499283E0EEL, 0xB952C623462A4332L,     // 6:D4
            0x74433EAD475B46A8L, 0x8B5EB112245FB4F8L,     // 6:D6
            0xA34B6478F0F61724L, 0x11A5DD7FFE6221FBL,     // 6:D8
            0xC16DA49D27CCBB4BL, 0x76A224D0BDE07301L,     // 6:DA
            0x8AA0BCA2598C2022L, 0x4DF336B86D90C48FL,     // 6:DC
            0xEA67663A740DB9E4L, 0xEF465F70E0B54771L,     // 6:DE
            0x39B008152ACB8227L, 0x7D1E5BF4F55E06ECL,     // 6:E0
            0x105BD0CF83B1B521L, 0x775C2960C033E7DBL,     // 6:E2
            0x7E014C397236A79FL, 0x811CC386113255CFL,     // 6:E4
            0xEDA7450D1A0E72D8L, 0x5889DF3D7A998F3BL,     // 6:E6
            0x2E2BFBEDC779FC3AL, 0xCE0EEF438619A4E9L,     // 6:E8
            0x372D4E7BF6CD095FL, 0x04DF34FAE96B6A4FL,     // 6:EA
            0xF923A13870D4ADB6L, 0xA1AA7E050A4D228DL,     // 6:EC
            0xA8F71B5CB84862C9L, 0xB52E9A306097FDE3L,     // 6:EE
            0x0D8251A35B6E2A0BL, 0x2257A7FEE1C442EBL,     // 6:F0
            0x73831D9A29588D94L, 0x51D4BA64C89CCF7FL,     // 6:F2
            0x502AB7D4B54F5BA5L, 0x97793DCE8153BF08L,     // 6:F4
            0xE5042DE4D5D8A646L, 0x9687307EFC802BD2L,     // 6:F6
            0xA05473B5779EB657L, 0xB4D097801D446939L,     // 6:F8
            0xCFF0E2F3FBCA3033L, 0xC38CBEE0DD778EE2L,     // 6:FA
            0x464F499C252EB162L, 0xCAD1DBB96F72CEA6L,     // 6:FC
            0xBA4DD1EEC142E241L, 0xB00FA37AF42F0376L      // 6:FE
            ,
            0xD01F715B5C7EF8E6L, 0x16FA240980778325L,     // 7:00
            0xA8A42E857EE049C8L, 0x6AC1068FA186465BL,     // 7:02
            0x6E417BD7A2E9320BL, 0x665C8167A437DAABL,     // 7:04
            0x7666681AA89617F6L, 0x4B959163700BDCF5L,     // 7:06
            0xF14BE6B78DF36248L, 0xC585BD689A625CFFL,     // 7:08
            0x9557D7FCA67D82CBL, 0x89F0B969AF6DD366L,     // 7:0A
            0xB0833D48749F6C35L, 0xA1998C23B1ECBC7CL,     // 7:0C
            0x8D70C431AC02A736L, 0xD6DFBC2FD0A8B69EL,     // 7:0E
            0x37AEB3E551FA198BL, 0x0B7D128A40B5CF9CL,     // 7:10
            0x5A8F2008B5780CBCL, 0xEDEC882284E333E5L,     // 7:12
            0xD25FC177D3C7C2CEL, 0x5E0F5D50B61778ECL,     // 7:14
            0x1D873683C0C24CB9L, 0xAD040BCBB45D208CL,     // 7:16
            0x2F89A0285B853C76L, 0x5732FFF6791B8D58L,     // 7:18
            0x3E9311439EF6EC3FL, 0xC9183A809FD3C00FL,     // 7:1A
            0x83ADF3F5260A01EEL, 0xA6791941F4E8EF10L,     // 7:1C
            0x103AE97D0CA1CD5DL, 0x2CE948121DEE1B4AL,     // 7:1E
            0x39738421DBF2BF53L, 0x093DA2A6CF0CF5B4L,     // 7:20
            0xCD9847D89CBCB45FL, 0xF9561C078B2D8AE8L,     // 7:22
            0x9C6A755A6971777FL, 0xBC1EBAA0712EF0C5L,     // 7:24
            0x72E61542ABF963A6L, 0x78BB5FDE229EB12EL,     // 7:26
            0x14BA94250FCEB90DL, 0x844D6697630E5282L,     // 7:28
            0x98EA08026A1E032FL, 0xF06BBEA144217F5CL,     // 7:2A
            0xDB6263D11CCB377AL, 0x641C314B2B8EE083L,     // 7:2C
            0x320E96AB9B4770CFL, 0x1EE7DEB986A96B85L,     // 7:2E
            0xE96CF57A878C47B5L, 0xFDD6615F8842FEB8L,     // 7:30
            0xC83862965601DD1BL, 0x2EA9F83E92572162L,     // 7:32
            0xF876441142FF97FCL, 0xEB2C455608357D9DL,     // 7:34
            0x5612A7E0B0C9904CL, 0x6C01CBFB2D500823L,     // 7:36
            0x4548A6A7FA037A2DL, 0xABC4C6BF388B6EF4L,     // 7:38
            0xBADE77D4FDF8BEBDL, 0x799B07C8EB4CAC3AL,     // 7:3A
            0x0C9D87E805B19CF0L, 0xCB588AAC106AFA27L,     // 7:3C
            0xEA0C1D40C1E76089L, 0x2869354A1E816F1AL,     // 7:3E
            0xFF96D17307FBC490L, 0x9F0A9D602F1A5043L,     // 7:40
            0x96373FC6E016A5F7L, 0x5292DAB8B3A6E41CL,     // 7:42
            0x9B8AE0382C752413L, 0x4F15EC3B7364A8A5L,     // 7:44
            0x3FB349555724F12BL, 0xC7C50D4415DB66D7L,     // 7:46
            0x92B7429EE379D1A7L, 0xD37F99611A15DFDAL,     // 7:48
            0x231427C05E34A086L, 0xA439A96D7B51D538L,     // 7:4A
            0xB403401077F01865L, 0xDDA2AEA5901D7902L,     // 7:4C
            0x0A5D4A9C8967D288L, 0xC265280ADF660F93L,     // 7:4E
            0x8BB0094520D4E94EL, 0x2A29856691385532L,     // 7:50
            0x42A833C5BF072941L, 0x73C64D54622B7EB2L,     // 7:52
            0x07E095624504536CL, 0x8A905153E906F45AL,     // 7:54
            0x6F6123C16B3B2F1FL, 0xC6E55552DC097BC3L,     // 7:56
            0x4468FEB133D16739L, 0xE211E7F0C7398829L,     // 7:58
            0xA2F96419F7879B40L, 0x19074BDBC3AD38E9L,     // 7:5A
            0xF4EBC3F9474E0B0CL, 0x43886BD376D53455L,     // 7:5C
            0xD8028BEB5AA01046L, 0x51F23282F5CDC320L,     // 7:5E
            0xE7B1C2BE0D84E16DL, 0x081DFAB006DEE8A0L,     // 7:60
            0x3B33340D544B857BL, 0x7F5BCABC679AE242L,     // 7:62
            0x0EDD37C48A08A6D8L, 0x81ED43D9A9B33BC6L,     // 7:64
            0xB1A3655EBD4D7121L, 0x69A1EEB5E7ED6167L,     // 7:66
            0xF6AB73D5C8F73124L, 0x1A67A3E185C61FD5L,     // 7:68
            0x2DC91004D43C065EL, 0x0240B02C8FB93A28L,     // 7:6A
            0x90F7F2B26CC0EB8FL, 0x3CD3A16F114FD617L,     // 7:6C
            0xAAE49EA9F15973E0L, 0x06C0CD748CD64E78L,     // 7:6E
            0xDA423BC7D5192A6EL, 0xC345701C16B41287L,     // 7:70
            0x6D2193EDE4821537L, 0xFCF639494190E3ACL,     // 7:72
            0x7C3B228621F1C57EL, 0xFB16AC2B0494B0C0L,     // 7:74
            0xBF7E529A3745D7F9L, 0x6881B6A32E3F7C73L,     // 7:76
            0xCA78D2BAD9B8E733L, 0xBBFE2FC2342AA3A9L,     // 7:78
            0x0DBDDFFECC6381E4L, 0x70A6A56E2440598EL,     // 7:7A
            0xE4D12A844BEFC651L, 0x8C509C2765D0BA22L,     // 7:7C
            0xEE8C6018C28814D9L, 0x17DA7C1F49A59E31L,     // 7:7E
            0x609C4C1328E194D3L, 0xB3E3D57232F44B09L,     // 7:80
            0x91D7AAA4A512F69BL, 0x0FFD6FD243DABBCCL,     // 7:82
            0x50D26A943C1FDE34L, 0x6BE15E9968545B4FL,     // 7:84
            0x94778FEA6FAF9FDFL, 0x2B09DD7058EA4826L,     // 7:86
            0x677CD9716DE5C7BFL, 0x49D5214FFFB2E6DDL,     // 7:88
            0x0360E83A466B273CL, 0x1FC786AF4F7B7691L,     // 7:8A
            0xA0B9D435783EA168L, 0xD49F0C035F118CB6L,     // 7:8C
            0x01205816C9D21D14L, 0xAC2453DD7D8F3D98L,     // 7:8E
            0x545217CC3F70AA64L, 0x26B4028E9489C9C2L,     // 7:90
            0xDEC2469FD6765E3EL, 0x04807D58036F7450L,     // 7:92
            0xE5F17292823DDB45L, 0xF30B569B024A5860L,     // 7:94
            0x62DCFC3FA758AEFBL, 0xE84CAD6C4E5E5AA1L,     // 7:96
            0xCCB81FCE556EA94BL, 0x53B282AE7A74F908L,     // 7:98
            0x1B47FBF74C1402C1L, 0x368EEBF39828049FL,     // 7:9A
            0x7AFBEFF2AD278B06L, 0xBE5E0A8CFE97CAEDL,     // 7:9C
            0xCFD8F7F413058E77L, 0xF78B2BC301252C30L,     // 7:9E
            0x4D555C17FCDD928DL, 0x5F2F05467FC565F8L,     // 7:A0
            0x24F4B2A21B30F3EAL, 0x860DD6BBECB768AAL,     // 7:A2
            0x4C750401350F8F99L, 0x0000000000000000L,     // 7:A4
            0xECCCD0344D312EF1L, 0xB5231806BE220571L,     // 7:A6
            0xC105C030990D28AFL, 0x653C695DE25CFD97L,     // 7:A8
            0x159ACC33C61CA419L, 0xB89EC7F872418495L,     // 7:AA
            0xA9847693B73254DCL, 0x58CF90243AC13694L,     // 7:AC
            0x59EFC832F3132B80L, 0x5C4FED7C39AE42C4L,     // 7:AE
            0x828DABE3EFD81CFAL, 0xD13F294D95ACE5F2L,     // 7:B0
            0x7D1B7A90E823D86AL, 0xB643F03CF849224DL,     // 7:B2
            0x3DF3F979D89DCB03L, 0x7426D836272F2DDEL,     // 7:B4
            0xDFE21E891FA4432AL, 0x3A136C1B9D99986FL,     // 7:B6
            0xFA36F43DCD46ADD4L, 0xC025982650DF35BBL,     // 7:B8
            0x856D3E81AADC4F96L, 0xC4A5E57E53B041EBL,     // 7:BA
            0x4708168B75BA4005L, 0xAF44BBE73BE41AA4L,     // 7:BC
            0x971767D029C4B8E3L, 0xB9BE9FEEBB939981L,     // 7:BE
            0x215497ECD18D9AAEL, 0x316E7E91DD2C57F3L,     // 7:C0
            0xCEF8AFE2DAD79363L, 0x3853DC371220A247L,     // 7:C2
            0x35EE03C9DE4323A3L, 0xE6919AA8C456FC79L,     // 7:C4
            0xE05157DC4880B201L, 0x7BDBB7E464F59612L,     // 7:C6
            0x127A59518318F775L, 0x332ECEBD52956DDBL,     // 7:C8
            0x8F30741D23BB9D1EL, 0xD922D3FD93720D52L,     // 7:CA
            0x7746300C61440AE2L, 0x25D4EAB4D2E2EEFEL,     // 7:CC
            0x75068020EEFD30CAL, 0x135A01474ACAEA61L,     // 7:CE
            0x304E268714FE4AE7L, 0xA519F17BB283C82CL,     // 7:D0
            0xDC82F6B359CF6416L, 0x5BAF781E7CAA11A8L,     // 7:D2
            0xB2C38D64FB26561DL, 0x34CE5BDF17913EB7L,     // 7:D4
            0x5D6FB56AF07C5FD0L, 0x182713CD0A7F25FDL,     // 7:D6
            0x9E2AC576E6C84D57L, 0x9AAAB82EE5A73907L,     // 7:D8
            0xA3D93C0F3E558654L, 0x7E7B92AAAE48FF56L,     // 7:DA
            0x872D8EAD256575BEL, 0x41C8DBFFF96C0E7DL,     // 7:DC
            0x99CA5014A3CC1E3BL, 0x40E883E930BE1369L,     // 7:DE
            0x1CA76E95091051ADL, 0x4E35B42DBAB6B5B1L,     // 7:E0
            0x05A0254ECABD6944L, 0xE1710FCA8152AF15L,     // 7:E2
            0xF22B0E8DCB984574L, 0xB763A82A319B3F59L,     // 7:E4
            0x63FCA4296E8AB3EFL, 0x9D4A2D4CA0A36A6BL,     // 7:E6
            0xE331BFE60EEB953DL, 0xD5BF541596C391A2L,     // 7:E8
            0xF5CB9BEF8E9C1618L, 0x46284E9DBC685D11L,     // 7:EA
            0x2074CFFA185F87BAL, 0xBD3EE2B6B8FCEDD1L,     // 7:EC
            0xAE64E3F1F23607B0L, 0xFEB68965CE29D984L,     // 7:EE
            0x55724FDAF6A2B770L, 0x29496D5CD753720EL,     // 7:F0
            0xA75941573D3AF204L, 0x8E102C0BEA69800AL,     // 7:F2
            0x111AB16BC573D049L, 0xD7FFE439197AAB8AL,     // 7:F4
            0xEFAC380E0B5A09CDL, 0x48F579593660FBC9L,     // 7:F6
            0x22347FD697E6BD92L, 0x61BC1405E13389C7L,     // 7:F8
            0x4AB5C975B9D9C1E1L, 0x80CD1BCF606126D2L,     // 7:FA
            0x7186FD78ED92449AL, 0x93971A882AABCCB3L,     // 7:FC
            0x88D0E17F66BFCE72L, 0x27945A985D5BD4D6L      // 7:FE
    };

    private static final long[][] sbob_rc64 = new long[][]{
            new long[]{
                    0xB1085BDA1ECADAE9L, 0xEBCB2F81C0657C1FL,     // 0:00
                    0x2F6A76432E45D016L, 0x714EB88D7585C4FCL,     // 0:02
                    0x4B7CE09192676901L, 0xA2422A08A460D315L,     // 0:04
                    0x05767436CC744D23L, 0xDD806559F2A64507L      // 0:06
            },
            new long[]{
                    0x6FA3B58AA99D2F1AL, 0x4FE39D460F70B5D7L,     // 1:00
                    0xF3FEEA720A232B98L, 0x61D55E0F16B50131L,     // 1:02
                    0x9AB5176B12D69958L, 0x5CB561C2DB0AA7CAL,     // 1:04
                    0x55DDA21BD7CBCD56L, 0xE679047021B19BB7L      // 1:06
            },
            new long[]{
                    0xF574DCAC2BCE2FC7L, 0x0A39FC286A3D8435L,     // 2:00
                    0x06F15E5F529C1F8BL, 0xF2EA7514B1297B7BL,     // 2:02
                    0xD3E20FE490359EB1L, 0xC1C93A376062DB09L,     // 2:04
                    0xC2B6F443867ADB31L, 0x991E96F50ABA0AB2L      // 2:06
            },
            new long[]{
                    0xEF1FDFB3E81566D2L, 0xF948E1A05D71E4DDL,     // 3:00
                    0x488E857E335C3C7DL, 0x9D721CAD685E353FL,     // 3:02
                    0xA9D72C82ED03D675L, 0xD8B71333935203BEL,     // 3:04
                    0x3453EAA193E837F1L, 0x220CBEBC84E3D12EL      // 3:06
            },
            new long[]{
                    0x4BEA6BACAD474799L, 0x9A3F410C6CA92363L,     // 4:00
                    0x7F151C1F1686104AL, 0x359E35D7800FFFBDL,     // 4:02
                    0xBFCD1747253AF5A3L, 0xDFFF00B723271A16L,     // 4:04
                    0x7A56A27EA9EA63F5L, 0x601758FD7C6CFE57L      // 4:06
            },
            new long[]{
                    0xAE4FAEAE1D3AD3D9L, 0x6FA4C33B7A3039C0L,     // 5:00
                    0x2D66C4F95142A46CL, 0x187F9AB49AF08EC6L,     // 5:02
                    0xCFFAA6B71C9AB7B4L, 0x0AF21F66C2BEC6B6L,     // 5:04
                    0xBF71C57236904F35L, 0xFA68407A46647D6EL      // 5:06
            },
            new long[]{
                    0xF4C70E16EEAAC5ECL, 0x51AC86FEBF240954L,     // 6:00
                    0x399EC6C7E6BF87C9L, 0xD3473E33197A93C9L,     // 6:02
                    0x0992ABC52D822C37L, 0x06476983284A0504L,     // 6:04
                    0x3517454CA23C4AF3L, 0x8886564D3A14D493L      // 6:06
            },
            new long[]{
                    0x9B1F5B424D93C9A7L, 0x03E7AA020C6E4141L,     // 7:00
                    0x4EB7F8719C36DE1EL, 0x89B4443B4DDBC49AL,     // 7:02
                    0xF4892BCB929B0690L, 0x69D18D2BD1A5C42FL,     // 7:04
                    0x36ACC2355951A8D9L, 0xA47F0DD4BF02E71EL      // 7:06
            },
            new long[]{
                    0x378F5A541631229BL, 0x944C9AD8EC165FDEL,     // 8:00
                    0x3A7D3A1B25894224L, 0x3CD955B7E00D0984L,     // 8:02
                    0x800A440BDBB2CEB1L, 0x7B2B8A9AA6079C54L,     // 8:04
                    0x0E38DC92CB1F2A60L, 0x7261445183235ADBL      // 8:06

            },
            new long[]{
                    0xABBEDEA680056F52L, 0x382AE548B2E4F3F3L,     // 9:00
                    0x8941E71CFF8A78DBL, 0x1FFFE18A1B336103L,     // 9:02
                    0x9FE76702AF69334BL, 0x7A1E6C303B7652F4L,     // 9:04
                    0x3698FAD1153BB6C3L, 0x74B4C7FB98459CEDL      // 9:06
            },
            new long[]{
                    0x7BCD9ED0EFC889FBL, 0x3002C6CD635AFE94L,     // 10:00
                    0xD8FA6BBBEBAB0761L, 0x2001802114846679L,     // 10:02
                    0x8A1D71EFEA48B9CAL, 0xEFBACD1D7D476E98L,     // 10:04
                    0xDEA2594AC06FD85DL, 0x6BCAA4CD81F32D1BL      // 10:06
            },
            new long[]{
                    0x378EE767F11631BAL, 0xD21380B00449B17AL,     // 11:00
                    0xCDA43C32BCDF1D77L, 0xF82012D430219F9BL,     // 11:02
                    0x5D80EF9D1891CC86L, 0xE71DA4AA88E12852L,     // 11:04
                    0xFAF417D5D9B21B99L, 0x48BC924AF11BD720L      // 11:06
            }

    };
}
