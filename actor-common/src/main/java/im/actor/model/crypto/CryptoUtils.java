package im.actor.model.crypto;

import im.actor.model.CryptoProvider;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class CryptoUtils {

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    private static CryptoProvider provider;

    public static void init(CryptoProvider provider) {
        CryptoUtils.provider = provider;
    }

    public static CryptoKeyPair generateRSA1024KeyPair() {
        return provider.generateRSA1024KeyPair();
    }

    public static byte[] MD5(byte[] data) {
        return provider.MD5(data);
    }

    /**
     * Calculating SHA256
     *
     * @param data source data
     * @return SHA256 of data
     */
    public static byte[] SHA256(byte[] data) {
        return provider.SHA256(data);
    }

    /**
     * Calculating SHA256
     *
     * @param data source data
     * @return SHA256 of data
     */
    public static byte[] SHA512(byte[] data) {
        return provider.SHA512(data);
    }

    /**
     * Generate securely random int
     *
     * @param maxValue maximum value of int
     * @return random value
     */
    public static int randomInt(int maxValue) {
        return provider.randomInt(maxValue);
    }

    /**
     * Generate securely some amount of bytes
     *
     * @param len bytes count
     * @return random bytes
     */
    public static byte[] randomBytes(int len) {
        return provider.randomBytes(len);
    }

    /**
     * Calculating lowcase hex string
     *
     * @param bytes data for hex
     * @return hex string
     */
    public static String hex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
