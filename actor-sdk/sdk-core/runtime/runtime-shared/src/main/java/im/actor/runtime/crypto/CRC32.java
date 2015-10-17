/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto;

/**
 * CRC32 Implementation
 */
public class CRC32 {
    /**
     * The crc data checksum so far.
     */
    private int crc = 0;

    /**
     * The fast CRC table. Computed once when the CRC32 class is loaded.
     */
    private static int[] crc_table = make_crc_table();

    /**
     * Make the table for a fast CRC.
     */
    private static int[] make_crc_table() {
        int[] crc_table = new int[256];
        for (int n = 0; n < 256; n++) {
            int c = n;
            for (int k = 8; --k >= 0; ) {
                if ((c & 1) != 0)
                    c = 0xedb88320 ^ (c >>> 1);
                else
                    c = c >>> 1;
            }
            crc_table[n] = c;
        }
        return crc_table;
    }

    /**
     * Returns the CRC32 data checksum computed so far.
     */
    public long getValue() {
        return (long) crc & 0xffffffffL;
    }

    /**
     * Resets the CRC32 data checksum as if no update was ever called.
     */
    public void reset() {
        crc = 0;
    }

    /**
     * Updates the checksum with the int bval.
     *
     * @param bval (the byte is taken as the lower 8 bits of bval)
     */

    public void update(int bval) {
        int c = ~crc;
        c = crc_table[(c ^ bval) & 0xff] ^ (c >>> 8);
        crc = ~c;
    }

    /**
     * Adds the byte array to the data checksum.
     *
     * @param buf the buffer which contains the data
     * @param off the offset in the buffer where the data starts
     * @param len the length of the data
     */
    public void update(byte[] buf, int off, int len) {
        int c = ~crc;
        while (--len >= 0)
            c = crc_table[(c ^ buf[off++]) & 0xff] ^ (c >>> 8);
        crc = ~c;
    }

    /**
     * Adds the complete byte array to the data checksum.
     */
    public void update(byte[] buf) {
        update(buf, 0, buf.length);
    }
}