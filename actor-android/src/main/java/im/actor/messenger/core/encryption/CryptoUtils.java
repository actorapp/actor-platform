package im.actor.messenger.core.encryption;

import im.actor.messenger.util.io.StreamingUtils;
import im.actor.messenger.util.support.Arrays;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;

public class CryptoUtils {

    private static final SecureRandom secure = new SecureRandom();

    public static byte[] generateSeed(int size) {
        synchronized (secure) {
            return secure.generateSeed(size);
        }
    }

    public static byte[] groupKeyHash(byte[] aesFullKey, byte[] rsaPrivate) {
        return CryptoUtils.SHA256(CryptoUtils.concat(aesFullKey, rsaPrivate));
    }

    public static long keyHash(PublicKey key) {
        byte[] keyHash = keySHA256(key);

        byte[] res = Arrays.copyOfRange(keyHash, 0, 8);
        bytesXor(res, keyHash, 8, 8);
        bytesXor(res, keyHash, 16, 8);
        bytesXor(res, keyHash, 24, 8);

        return StreamingUtils.readLong(res, 0);
    }

    private static byte[] keySHA256(PublicKey key) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No SHA-256 available");
        }
    }

    public static byte[] SHA256(byte[] data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No SHA-256 available");
        }
    }

    private static void bytesXor(byte[] res, byte[] b, int bOffset, int bLength) {
        if (res == null || b == null || res.length != bLength) {
            throw new IllegalArgumentException();
        }

        int resIndex = 0;
        for (int i = bOffset; i < bOffset + bLength; ++i) {
            res[resIndex] = (byte) (res[resIndex] ^ b[i]);
            resIndex++;
        }
    }

    public static byte[] concat(byte[]... v) {
        int len = 0;
        for (int i = 0; i < v.length; i++) {
            len += v[i].length;
        }
        byte[] res = new byte[len];
        int offset = 0;
        for (int i = 0; i < v.length; i++) {
            System.arraycopy(v[i], 0, res, offset, v[i].length);
            offset += v[i].length;
        }
        return res;
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
}