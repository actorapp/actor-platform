package im.actor.crypto;

import im.actor.crypto.primitives.modes.CBCBlockCipher;
import im.actor.crypto.primitives.kuznechik.KuznechikCipher;

import java.security.SecureRandom;

/**
 * Kuznechik encryption with BCB chipher operation mode
 * <p/>
 * Ported by Steven Kite (steve@actor.im) from
 * https://github.com/mjosaarinen/kuznechik/blob/master/kuznechik_8bit.c
 */
public class Kuznechik {

    private SecureRandom random = new SecureRandom();

    /**
     * Encrypting data with key
     *
     * @param key  key for encryption
     * @param iv   initialization vector
     * @param data data for encryption
     * @return encrypted data
     */
    public byte[] encrypt(byte[] key, byte[] iv, byte[] data) {
        CBCBlockCipher cbcBlockCipher = new CBCBlockCipher(new KuznechikCipher(key));
        return cbcBlockCipher.encrypt(iv, data);
    }

    /**
     * Decrypting data with key
     *
     * @param key  key for decryption
     * @param iv   initialization vector
     * @param data data for decryption
     * @return decrypted data
     */
    public byte[] decrypt(byte[] key, byte[] iv, byte[] data) {
        CBCBlockCipher cbcBlockCipher = new CBCBlockCipher(new KuznechikCipher(key));
        return cbcBlockCipher.decrypt(iv, data);
    }

    /**
     * Key generation
     *
     * @return generated key
     */
    public KuznechikKey keyGen() {
        byte[] key = new byte[32];
        random.nextBytes(key);
        return new KuznechikKey(key);
    }
}
