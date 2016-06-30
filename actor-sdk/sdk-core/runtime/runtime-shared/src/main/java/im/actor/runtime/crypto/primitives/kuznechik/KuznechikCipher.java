package im.actor.runtime.crypto.primitives.kuznechik;

import im.actor.runtime.crypto.BlockCipher;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ALL_CHECKS 1
]-*/

/**
 * Kuzhnechik (aka Grasshopper) russian encryption standard implementation
 * <p>
 * Ported by Steven Kite (steve@actor.im) from
 * https://github.com/mjosaarinen/kuznechik/blob/master/kuznechik_8bit.c
 */
public class KuznechikCipher implements BlockCipher {

    private static final int BLOCK_SIZE = 16;
    private byte[] rawKey;
    private KuzIntKey key;

    public KuznechikCipher(byte[] rawKey) {
        this.rawKey = rawKey;
        this.key = convertKey(rawKey);
    }

    /**
     * Encrypting Block with KuznechikImpl encryption
     *
     * @param data       16-byte block for encryption
     * @param offset     offset in block
     * @param dest       destination array
     * @param destOffset destinaation offset
     */
    public void encryptBlock(byte[] data, int offset, byte[] dest, int destOffset) {
        // w128_t x;
        // x.q[0] = ((uint64_t *) blk)[0];
        // x.q[1] = ((uint64_t *) blk)[1];
        Kuz128 x = new Kuz128();
        x.setQ(0, ByteStrings.bytesToLong(data, offset));
        x.setQ(1, ByteStrings.bytesToLong(data, offset + 8));

        for (int i = 0; i < 9; i++) {
            // x.q[0] ^= key->k[i].q[0];
            // x.q[1] ^= key->k[i].q[1];
            x.setQ(0, x.getQ(0) ^ key.getK()[i].getQ(0));
            x.setQ(1, x.getQ(1) ^ key.getK()[i].getQ(1));

            for (int j = 0; j < 16; j++) {
                // x.b[j] = kuz_pi[x.b[j]];
                x.getB()[j] = KuznechikTables.kuz_pi[(x.getB()[j] & 0xFF)];
            }
            // kuz_l(&x);
            KuznechikMath.kuz_l(x);
        }

        // ((uint64_t *) blk)[0] = x.q[0] ^ key->k[9].q[0];
        // ((uint64_t *) blk)[1] = x.q[1] ^ key->k[9].q[1];
        ByteStrings.write(dest, destOffset, ByteStrings.longToBytes(x.getQ(0) ^ key.getK()[9].getQ(0)), 0, 8);
        ByteStrings.write(dest, destOffset + 8, ByteStrings.longToBytes(x.getQ(1) ^ key.getK()[9].getQ(1)), 0, 8);
    }

    /**
     * Decrypting block with Kuznechik encryption
     *
     * @param data       source data for decryption
     * @param offset     offset in data
     * @param dest       destination array
     * @param destOffset destination offset
     */
    @Override
    public void decryptBlock(byte[] data, int offset, byte[] dest, int destOffset) {
        // w128_t x;
        // x.q[0] = ((uint64_t *) blk)[0] ^ key->k[9].q[0];
        // x.q[1] = ((uint64_t *) blk)[1] ^ key->k[9].q[1];
        Kuz128 x = new Kuz128();
        x.setQ(0, ByteStrings.bytesToLong(data, offset) ^ key.getK()[9].getQ(0));
        x.setQ(1, ByteStrings.bytesToLong(data, offset + 8) ^ key.getK()[9].getQ(1));

        for (int i = 8; i >= 0; i--) {
            // kuz_l_inv(&x);
            KuznechikMath.kuz_l_inv(x);

            for (int j = 0; j < 16; j++) {
                // x.b[j] = kuz_pi_inv[x.b[j]];
                x.getB()[j] = KuznechikTables.kuz_pi_inv[x.getB()[j] & 0xFF];
            }

            // x.q[0] ^= key->k[i].q[0];
            x.setQ(0, x.getQ(0) ^ key.getK()[i].getQ(0));
            // x.q[1] ^= key->k[i].q[1];
            x.setQ(1, x.getQ(1) ^ key.getK()[i].getQ(1));
        }

        // ((uint64_t *) blk)[0] = x.q[0];
        // ((uint64_t *) blk)[1] = x.q[1];
        ByteStrings.write(dest, destOffset, ByteStrings.longToBytes(x.getQ(0)), 0, 8);
        ByteStrings.write(dest, destOffset + 8, ByteStrings.longToBytes(x.getQ(1)), 0, 8);
    }

    /**
     * Raw Key
     *
     * @return key
     */
    public byte[] getRawKey() {
        return rawKey;
    }

    @Override
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    /**
     * Converting binary representation of a key to internal format
     *
     * @param key raw key
     * @return key in internal format
     */
    static KuzIntKey convertKey(byte[] key) {
        if (key.length != 32) {
            throw new RuntimeException("Key might be 32 bytes length");
        }

        KuzIntKey kuz = new KuzIntKey();
        // w128_t c, x, y, z;
        Kuz128 c = new Kuz128(), x = new Kuz128(), y = new Kuz128(), z = new Kuz128();

        for (int i = 0; i < 16; i++) {
            // this will be have to changed for little-endian systems
            // x.b[i] = key[i];
            // y.b[i] = key[i + 16];
            x.getB()[i] = key[i];
            y.getB()[i] = key[i + 16];
        }

        // kuz->k[0].q[0] = x.q[0];
        // kuz->k[0].q[1] = x.q[1];
        // kuz->k[1].q[0] = y.q[0];
        // kuz->k[1].q[1] = y.q[1];
        kuz.getK()[0].set(x);
        kuz.getK()[1].set(y);

        for (int i = 1; i <= 32; i++) {
            // C Value
            // c.q[0] = 0;
            // c.q[1] = 0;
            // c.b[15] = i;		// load round in lsb
            // kuz_l(&c);
            c.setQ(0, 0);
            c.setQ(1, 0);
            c.getB()[15] = (byte) i;
            KuznechikMath.kuz_l(c);

            // z.q[0] = x.q[0] ^ c.q[0];
            // z.q[1] = x.q[1] ^ c.q[1];
            // for (j = 0; j < 16; j++)
            //   z.b[j] = kuz_pi[z.b[j]];
            // kuz_l(&z);
            z.setQ(0, x.getQ(0) ^ c.getQ(0));
            z.setQ(1, x.getQ(1) ^ c.getQ(1));
            for (int j = 0; j < 16; j++) {
                z.getB()[j] = KuznechikTables.kuz_pi[(z.getB()[j] & 0xFF)];
            }
            KuznechikMath.kuz_l(z);

            // z.q[0] ^= y.q[0];
            // z.q[1] ^= y.q[1];
            z.setQ(0, z.getQ(0) ^ y.getQ(0));
            z.setQ(1, z.getQ(1) ^ y.getQ(1));

            // y.q[0] = x.q[0];
            // y.q[1] = x.q[1];
            y.set(x);

            // x.q[0] = z.q[0];
            // x.q[1] = z.q[1];
            x.set(z);

            // if ((i & 7) == 0) {
            //    kuz->k[(i >> 2)].q[0] = x.q[0];
            //    kuz->k[(i >> 2)].q[1] = x.q[1];
            //    kuz->k[(i >> 2) + 1].q[0] = y.q[0];
            //    kuz->k[(i >> 2) + 1].q[1] = y.q[1];
            // }
            if ((i & 7) == 0) {
                kuz.getK()[(i >> 2)].set(x);
                kuz.getK()[(i >> 2) + 1].set(y);
            }
        }

        return kuz;
    }
}
