package im.actor.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

/**
 * Created by ex3ndr on 12.10.14.
 */
class Utils {

    public static byte[][] split(byte[] data, int splitSize1, int splitSize2) {
        if (data.length < splitSize1 + splitSize2) {
            throw new IllegalArgumentException("data.length must be >= splitSize1 + splitSize2");
        }

        byte[] a = new byte[splitSize1];
        byte[] b = new byte[splitSize2];
        System.arraycopy(data, 0, a, 0, a.length);
        System.arraycopy(data, splitSize1, b, 0, b.length);
        return new byte[][]{a, b};
    }

    public static byte[][] split(byte[] data, int splitSize1, int splitSize2, int splitSize3) {
        if (data.length < splitSize1 + splitSize2 + splitSize3) {
            throw new IllegalArgumentException("data.length must be >= splitSize1 + splitSize2 + splitSize3");
        }

        byte[] a = new byte[splitSize1];
        byte[] b = new byte[splitSize2];
        byte[] c = new byte[splitSize3];
        System.arraycopy(data, 0, a, 0, a.length);
        System.arraycopy(data, splitSize1, b, 0, b.length);
        System.arraycopy(data, splitSize1 + splitSize2, c, 0, c.length);
        return new byte[][]{a, b, c};
    }

    public static byte[] substring(byte[] src, int start, int len) {
        byte[] res = new byte[len];
        System.arraycopy(src, start, res, 0, len);
        return res;
    }

    public static byte[] align(byte[] src, int factor) {
        if (src.length % factor == 0) {
            return src;
        }
        int padding = factor - src.length % factor;

        return concat(src, new byte[padding]);
    }

    public static byte[] concat(byte[]... v) {
        int len = 0;
        for (byte[] aV : v) {
            len += aV.length;
        }
        byte[] res = new byte[len];
        int offset = 0;
        for (byte[] aV : v) {
            System.arraycopy(aV, 0, res, offset, aV.length);
            offset += aV.length;
        }
        return res;
    }


    public static boolean equals(byte[] src, byte[] dst) {
        if (src.length != dst.length) {
            return false;
        }
        for (int i = 0; i < dst.length; i++) {
            if (src[i] != dst[i]) {
                return false;
            }
        }

        return true;
    }

    public static byte[] readBytes(int count, InputStream stream) throws IOException {
        byte[] res = new byte[count];
        int offset = 0;
        while (offset < res.length) {
            int readed = stream.read(res, offset, res.length - offset);
            if (readed > 0) {
                offset += readed;
            } else if (readed < 0) {
                throw new IOException();
            } else {
                Thread.yield();
            }
        }
        return res;
    }

    public static int readInt(byte[] bytes, int offset) {
        int a = bytes[offset] & 0xFF;
        int b = bytes[offset + 1] & 0xFF;
        int c = bytes[offset + 2] & 0xFF;
        int d = bytes[offset + 3] & 0xFF;

        return d + (c << 8) + (b << 16) + (a << 24);
    }
}
