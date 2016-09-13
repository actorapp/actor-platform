package im.actor.keygen;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import im.actor.keygen.curve25519.curve_sigs;

public final class Curve25519 {

    /**
     * Generating KeyPair
     *
     * @param randomBytes 32 random bytes
     * @return generated key pair
     */
    public static Curve25519KeyPair keyGen(byte[] randomBytes) throws NoSuchAlgorithmException, DigestException {
        byte[] privateKey = keyGenPrivate(randomBytes);
        byte[] publicKey = keyGenPublic(privateKey);
        return new Curve25519KeyPair(publicKey, privateKey);
    }

    /**
     * Generating private key. Source: https://cr.yp.to/ecdh.html
     *
     * @param randomBytes random bytes (32+ bytes)
     * @return generated private key
     */
    public static byte[] keyGenPrivate(byte[] randomBytes) throws NoSuchAlgorithmException, DigestException {

        if (randomBytes.length < 32) {
            throw new RuntimeException("Random bytes too small");
        }

        // Hashing Random Bytes instead of using random bytes directly
        // Just in case as reference ed255519 implementation do same
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.digest(randomBytes, 0, randomBytes.length);
        byte[] privateKey = digest.digest();

        // Performing bit's flipping
        privateKey[0] &= 248;
        privateKey[31] &= 127;
        privateKey[31] |= 64;

        return privateKey;
    }

    /**
     * Building public key with private key
     *
     * @param privateKey private key
     * @return generated public key
     */
    public static byte[] keyGenPublic(byte[] privateKey) {
        byte[] publicKey = new byte[32];
        curve_sigs.curve25519_keygen(publicKey, privateKey);
        return publicKey;
    }

    private Curve25519() {

    }
}