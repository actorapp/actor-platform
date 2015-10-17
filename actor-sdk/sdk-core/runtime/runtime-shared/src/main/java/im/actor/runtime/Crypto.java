/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import java.math.BigInteger;

public class Crypto {

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    private static final CryptoRuntime provider = new CryptoRuntimeProvider();
    private static final RandomRuntime random = new RandomRuntimeProvider();

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
        return random.randomInt(maxValue);
    }

    /**
     * Generate securely some amount of bytes
     *
     * @param len bytes count
     * @return random bytes
     */
    public static byte[] randomBytes(int len) {
        return random.randomBytes(len);
    }

    public static void nextBytes(byte[] data) {
        random.nextBytes(data);
    }

    public static BigInteger generateBigInteger(int numBits) {
        return random.generateBigInteger(numBits);
    }

    public static BigInteger generateBigInteger(int numBits, int certanity) {
        return random.generateBigInteger(numBits, certanity);
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

    private static int fromHexShort(char a) {
        if (a >= '0' && a <= '9') {
            return a - '0';
        }
        if (a >= 'a' && a <= 'f') {
            return 10 + (a - 'a');
        }

        throw new RuntimeException();
    }

    public static byte[] fromHex(String hex) {
        byte[] res = new byte[hex.length() / 2];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) ((fromHexShort(hex.charAt(i * 2)) << 4) + fromHexShort(hex.charAt(i * 2 + 1)));
        }
        return res;
    }
}
