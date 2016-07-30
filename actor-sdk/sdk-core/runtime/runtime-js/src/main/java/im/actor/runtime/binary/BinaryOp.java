package im.actor.runtime.binary;

public class BinaryOp {

    public static void intToBigEndian(int n, byte[] bs, int off) {
        bs[off] = jsWrap((byte) (n >>> 24));
        bs[++off] = jsWrap((byte) (n >>> 16));
        bs[++off] = jsWrap((byte) (n >>> 8));
        bs[++off] = jsWrap((byte) (n));
    }

    public static void intToLittleEndian(int n, byte[] bs, int off) {
        bs[off] = jsWrap((byte) (n));
        bs[++off] = jsWrap((byte) (n >>> 8));
        bs[++off] = jsWrap((byte) (n >>> 16));
        bs[++off] = jsWrap((byte) (n >>> 24));
    }

    public static int jsWrap(int val) {
        return val & 0xFFFFFFFF;
    }

    public static byte jsWrap(byte val) {
        return (byte) (val & 0xFF);
    }
}
