package im.actor.runtime.crypto.primitives.streebog;

import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ALL_CHECKS 1
]-*/

/**
 * 512-bit variable
 */
public class Int512 {

    private byte[] value;

    public Int512(byte[] value) {
        setBytes(value);
    }

    public Int512() {
        this.value = new byte[64];
    }

    public byte[] getBytes() {
        return value;
    }

    public void setBytes(byte[] value) {
        this.value = new byte[64];
        for (int i = 0; i < 64; i++) {
            this.value[i] = (byte) (value[i] & 0xFF);
        }
    }

    public byte getByte(int index) {
        return value[index];
    }

    public void setByte(int index, byte v) {
        value[index] = v;
    }

    public int getWord16(int index) {
        return (value[index * 2] & 0xFF) + (((value[index * 2 + 1] & 0xFF) << 8) & 0xFFFF);
    }

    public void setWord16(int index, int val) {
        value[index * 2] = (byte) (val & 0xFF);
        value[index * 2 + 1] = (byte) ((val & 0xFF) >> 8);
    }

    public long getWord64(int index) {
        return ByteStrings.bytesToLong(value, index * 8);
    }

    public void setWord64(int index, long val) {
        ByteStrings.write(value, index * 8, ByteStrings.longToBytes(val), 0, 8);
    }
}
