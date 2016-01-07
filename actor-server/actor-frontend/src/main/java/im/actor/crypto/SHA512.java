package im.actor.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA512 {
    public static byte[] calc(byte[] arg, int length) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("SHA-256 is not available");
        }
        md.update(arg, 0, length);
        return md.digest();
    }

    public static byte[] calc(byte[]... arg) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("SHA-256 is not available");
        }
        for (byte[] a : arg) {
            md.update(a);
        }
        return md.digest();
    }
}
