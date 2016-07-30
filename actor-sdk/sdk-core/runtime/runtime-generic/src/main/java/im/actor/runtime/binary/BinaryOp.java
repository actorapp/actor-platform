package im.actor.runtime.binary;

public class BinaryOp {

    public static void intToBigEndian(int n, byte[] bs, int off) {
        bs[off] = (byte) (n >>> 24);
        bs[++off] = (byte) (n >>> 16);
        bs[++off] = (byte) (n >>> 8);
        bs[++off] = (byte) (n);
    }

    public static void intToLittleEndian(int n, byte[] bs, int off) {
        bs[off] = (byte) (n);
        bs[++off] = (byte) (n >>> 8);
        bs[++off] = (byte) (n >>> 16);
        bs[++off] = (byte) (n >>> 24);
    }
}
