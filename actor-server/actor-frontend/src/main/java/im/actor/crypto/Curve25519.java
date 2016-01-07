package im.actor.crypto;

import im.actor.crypto.primitives.curve25519.Sha512;
import im.actor.crypto.primitives.curve25519.curve_sigs;
import im.actor.crypto.primitives.curve25519.scalarmult;

import java.security.SecureRandom;

public class Curve25519 {

    private SecureRandom random = new SecureRandom();

    /**
     * Generating KeyPair
     *
     * @return generated key pair
     */
    public synchronized Curve25519KeyPair keyGen() {
        byte[] privateKey = keyGenPrivate();
        byte[] publicKey = keyGenPublic(privateKey);
        return new Curve25519KeyPair(publicKey, privateKey);
    }

    /**
     * Generating private key. Source: https://cr.yp.to/ecdh.html
     *
     * @return generated private key
     */
    public synchronized byte[] keyGenPrivate() {

        // Hashing Random Bytes instead of using random bytes directly
        // Just in case as reference ed255519 implementation do same
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        byte[] privateKey = SHA256.calc(randomBytes);

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
    public synchronized byte[] keyGenPublic(byte[] privateKey) {
        byte[] publicKey = new byte[32];
        curve_sigs.curve25519_keygen(publicKey, privateKey);
        return publicKey;
    }

    /**
     * Calculating DH agreement
     *
     * @param ourPrivate  Our Private Key
     * @param theirPublic Theirs Public key
     * @return calculated agreement
     */
    public synchronized byte[] calculateAgreement(byte[] ourPrivate, byte[] theirPublic) {
        byte[] agreement = new byte[32];
        scalarmult.crypto_scalarmult(agreement, ourPrivate, theirPublic);
        return agreement;
    }

    /**
     * Calculating signature
     *
     * @param random     random seed for signature
     * @param privateKey private key for signature
     * @param message    message to sign
     * @return signature
     */
    public synchronized byte[] calculateSignature(byte[] random, byte[] privateKey, byte[] message) {
        byte[] result = new byte[64];

        if (curve_sigs.curve25519_sign(SHA512Provider, result, privateKey, message, message.length, random) != 0) {
            throw new IllegalArgumentException("Message exceeds max length!");
        }

        return result;
    }

    /**
     * Verification of signature
     *
     * @param publicKey public key of signature
     * @param message   message
     * @param signature signature of a message
     * @return true if signature correct
     */
    public synchronized boolean verifySignature(byte[] publicKey, byte[] message, byte[] signature) {
        return curve_sigs.curve25519_verify(SHA512Provider, signature, publicKey, message, message.length) == 0;
    }

    private final Sha512 SHA512Provider = new Sha512() {
        @Override
        public void calculateDigest(byte[] out, byte[] in, long length) {
            byte[] res = SHA512.calc(in, (int) length);
            for (int i = 0; i < 64; i++) {
                out[i] = res[i];
            }
        }
    };
}