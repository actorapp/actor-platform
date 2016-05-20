/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import im.actor.runtime.crypto.BlockCipher;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.primitives.digest.KeyDigest;
import im.actor.runtime.crypto.primitives.digest.MD5;
import im.actor.runtime.util.Hex;

public class Crypto {

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    private static final CryptoRuntime runtime = new CryptoRuntimeProvider();

    private static final RandomRuntime random = new RandomRuntimeProvider();

    public static void waitForCryptoLoaded() {
        runtime.waitForCryptoLoaded();
    }

    public static Digest createSHA256() {
        return runtime.SHA256();
    }

    public static BlockCipher createAES128(byte[] key) {
        return runtime.AES128(key);
    }

    public static byte[] MD5(byte[] data) {
        MD5 md5 = new MD5();
        md5.update(data, 0, data.length);
        byte[] res = new byte[16];
        md5.doFinal(res, 0);
        return res;
    }

    public static String keyHash(byte[] publicKey) {
        KeyDigest keyDigest = new KeyDigest();
        keyDigest.update(publicKey, 0, publicKey.length);
        byte[] res = new byte[8];
        keyDigest.doFinal(res, 0);
        return Hex.toHex(res);
    }

    /**
     * Calculating SHA256
     *
     * @param data source data
     * @return SHA256 of data
     */
    public static byte[] SHA256(byte[] data) {
        Digest sha256 = createSHA256();
        sha256.update(data, 0, data.length);
        byte[] res = new byte[32];
        sha256.doFinal(res, 0);
        return res;
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

    /**
     * Calculating lowcase hex string
     *
     * @param bytes data for hex
     * @return hex string
     */
    public static String hex(byte[] bytes) {
        return Hex.hex(bytes);
    }

    public static byte[] fromHex(String hex) {
        return Hex.fromHex(hex);
    }
}
