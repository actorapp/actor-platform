package im.actor.crypto;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static im.actor.crypto.Utils.concat;

/**
 * Class contains methods for creating all types of Ciphers and SecureRandom instances
 * for message and file encryption
 * <p/>
 * Most parameters are taken from WhisperSystem's TextSecure.
 */
public class Crypto {

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    static {
        PRNGFixes.apply();
    }

    /**
     * Creating SecureRandom instance
     *
     * @return the SecureRandom
     */
    public static SecureRandom getSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creating AES-CBC with PKCS#5 Padding scheme.
     * This mode used in many places for file and message encryption
     *
     * @return AES Cipher
     */
    public static Cipher createAESCipher() {
        try {
            return Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (NoSuchPaddingException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creating RSA with OAEP-SHA1-MGF1 Padding scheme
     * This cipher used for encryption of AES key of message
     *
     * @return the RSA Cipher
     */
    public static Cipher createRSACipher() {
        try {
            return Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creating HMAC-SHA256.
     * Used in file encryption
     *
     * @return the HMAC-SHA256
     */
    public static Mac createHmacSHA256() {
        try {
            return Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Generating of secure seed
     *
     * @param size seed zie
     * @return generated random bytes
     */
    public static byte[] generateSeed(int size) {
        byte[] key = new byte[size];
        getSecureRandom().nextBytes(key);
        return key;
    }

    /**
     * Calculating SHA256
     *
     * @param data source data
     * @return SHA256 of data
     */
    public static byte[] SHA256(byte[] data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No SHA-256 available");
        }
    }

    /**
     * Calculating SHA1
     *
     * @param data source data
     * @return SHA1 of data
     */
    public static byte[] SHA1(byte[] data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No SHA-1 available");
        }
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
