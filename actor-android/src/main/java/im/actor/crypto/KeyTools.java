package im.actor.crypto;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Helper class for storing and generating key material
 */
public class KeyTools {
    /**
     * Build X.509 Encoded public key
     *
     * @param publicKey public key
     * @return encoded key
     */
    public static byte[] encodeRsaPublicKey(PublicKey publicKey) {
        return publicKey.getEncoded();
    }

    /**
     * Decode X.509 encoded public key
     *
     * @param encoded encoded key
     * @return public key
     */
    public static PublicKey decodeRsaPublicKey(byte[] encoded) {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(encoded));
        } catch (InvalidKeySpecException e) {
            throw new AssertionError(e);
        }
    }


    /**
     * Build PKCS#8 Encoded private key
     *
     * @param privateKey private key
     * @return encoded key
     */
    public static byte[] encodeRsaPrivateKey(PrivateKey privateKey) {
        return privateKey.getEncoded();
    }

    /**
     * Decoding PKCS#8 Encoded private key
     *
     * @param encoded encoded key
     * @return private key
     */
    public static PrivateKey decodeRsaPrivateKey(byte[] encoded) {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        try {
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
        } catch (InvalidKeySpecException e) {
            throw new AssertionError(e);
        }
    }


    /**
     * Generation of 1024-bit RSA key pair
     *
     * @return RSA key pair
     */
    public static KeyPair generateNewRsaKey() {
        KeyPairGenerator g = null;
        try {
            g = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        g.initialize(Config.RSA_SIZE);
        return g.generateKeyPair();
    }
}
