package im.actor.runtime.crypto.primitives.streebog;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ALL_CHECKS 1
]-*/

public class StreebogDigest {

    private final int hashLength;
    private Int512 h = new Int512();
    private Int512 m = new Int512();
    private Int512 e = new Int512();
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
                h.setByte(i, (byte) 0x01);
            } else {
                h.setByte(i, (byte) 0x00);
            }
            e.setByte(i, (byte) 0x00);
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
            m.setByte(j--, (byte) (in[offset + i] & 0xFF));

            // compress
            // if (j < 0) {
            if (j < 0) {
                // streebog_g(&sbx->h, &sbx->m, sbx->n);
                StreebogMath.streebog_g(h, m, n);
                // sbx->n += 0x200;
                n += 0x200;

                // epsilon summation
                // c = 0;
                int c = 0;

                // for (j = 63; j >= 0; j--) {
                for (j = 63; j >= 0; j--) {
                    // c += sbx->e.b[j] + sbx->m.b[j];
                    c += (e.getByte(j) & 0xFF) + (m.getByte(j) & 0xFF);
                    // sbx->e.b[j] = c & 0xFF;
                    e.setByte(j, (byte) (c & 0xFF));
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
        m.setByte(i--, (byte) 0x01);
        while (i >= 0) {
            // sbx->m.b[i--] = 0x00;
            m.setByte(i--, (byte) 0x00);
        }
        // streebog_g(&sbx->h, &sbx->m, sbx->n);
        StreebogMath.streebog_g(h, m, n);

        // epsilon summation
        int c = 0;
        for (i = 63; i >= 0; i--) {
            // c += sbx->e.b[i] + sbx->m.b[i];
            c += (e.getByte(i) & 0xFF) + (m.getByte(i) & 0xFF);
            // sbx->e.b[i] = c & 0xFF;
            e.setByte(i, (byte) (c & 0xFF));
            // c >>= 8;
            c = c >> 8;
        }

        // finalization n
        // memset(&sbx->m, 0x00, 64);
        for (int j = 0; j < 64; j++) {
            m.setByte(j, (byte) 0x00);
        }

        // sbx->n += (63 - sbx->pt) << 3;      // total bits
        n += (63 - pt) << 3;
        for (i = 63; n > 0; i--) {
            // sbx->m.b[i] = sbx->n & 0xFF;
            m.setByte(i, (byte) (n & 0xFF));
            // sbx->n >>= 8;
            n = n >> 8;
        }

        // streebog_g(&sbx->h, &sbx->m, 0);
        // streebog_g(&sbx->h, &sbx->e, 0);
        StreebogMath.streebog_g(h, m, 0);
        StreebogMath.streebog_g(h, e, 0);

        // copy the result
        // memcpy(hash, &sbx->h, sbx->hlen);
        for (int j = 0; j < hashLength; j++) {
            out[offset + j] = h.getByte(j);
        }

        // clear out sensitive stuff
        reset();
    }
}
