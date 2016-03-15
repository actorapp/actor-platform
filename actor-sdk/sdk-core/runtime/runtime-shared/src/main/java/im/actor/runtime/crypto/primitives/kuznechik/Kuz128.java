package im.actor.runtime.crypto.primitives.kuznechik;

import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ALL_CHECKS 1
]-*/

/**
 * 128-bit variable
 */
class Kuz128 {

    private final byte[] b;

    public Kuz128(byte[] b) {
        this.b = b;
    }

    public Kuz128() {
        this.b = new byte[16];
    }

    public long getQ(int index) {
        return ByteStrings.bytesToLong(b, index * 8);
    }

    public void setQ(int index, long l) {
        byte[] data = ByteStrings.longToBytes(l);
        for (int i = 0; i < 8; i++) {
            b[i + index * 8] = data[i];
        }
    }

    public void set(Kuz128 kuz128) {
        for (int i = 0; i < 16; i++) {
            b[i] = kuz128.b[i];
        }
    }

    public byte[] getB() {
        return b;
    }
}
