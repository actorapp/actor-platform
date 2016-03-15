package im.actor.runtime.crypto.primitives.modes;

import com.google.j2objc.annotations.AutoreleasePool;

import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.BlockCipher;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

/**
 * CBC Cipher implementation
 * by Steve Kite (steve@actor.im)
 */
public class CBCBlockCipher {

    private final BlockCipher blockCipher;
    private final int blockSize;

    /**
     * Constructing block cipher
     *
     * @param blockCipher Underlying block cipher
     */
    public CBCBlockCipher(BlockCipher blockCipher) {
        this.blockCipher = blockCipher;
        this.blockSize = blockCipher.getBlockSize();
    }

    /**
     * Encrypting data
     *
     * @param iv   initialization vector
     * @param data data for encryption
     * @return encrypted data
     */
    public byte[] encrypt(byte[] iv, byte[] data) throws IntegrityException {
        if (data.length % blockSize != 0) {
            throw new IntegrityException("Incorrect data size");
        }
        if (iv.length != blockSize) {
            throw new IntegrityException("Incorrect iv size");
        }

        byte[] res = new byte[data.length];
        encrypt(iv, data, res);
        return res;
    }

    /**
     * Decrypting data
     *
     * @param iv   initialization vector
     * @param data data for decryption
     * @return decrypted data
     */
    public byte[] decrypt(byte[] iv, byte[] data) throws IntegrityException {
        if (data.length % blockSize != 0) {
            throw new IntegrityException("Incorrect data size");
        }
        if (iv.length != blockSize) {
            throw new IntegrityException("Incorrect iv size");
        }

        byte[] res = new byte[data.length];
        decrypt(iv, data, res);
        return res;
    }

    @AutoreleasePool
    private void encrypt(byte[] iv, byte[] data, byte[] res) {
        byte[] currentBlock = new byte[blockSize];

        byte[] currentIV = new byte[blockSize];
        for (int i = 0; i < blockSize; i++) {
            currentIV[i] = iv[i];
        }

        int count = data.length / blockSize;
        for (int i = 0; i < count; i++) {

            for (int j = 0; j < blockSize; j++) {
                currentBlock[j] = (byte) ((data[i * blockSize + j] & 0xFF) ^ (currentIV[j] & 0xFF));
            }

            blockCipher.encryptBlock(currentBlock, 0, res, i * blockSize);

            for (int j = 0; j < blockSize; j++) {
                currentIV[j] = res[i * blockSize + j];
            }
        }
    }

    @AutoreleasePool
    private void decrypt(byte[] iv, byte[] data, byte[] res) {
        byte[] r = new byte[blockSize];
        for (int i = 0; i < blockSize; i++) {
            r[i] = iv[i];
        }

        int count = data.length / blockSize;
        for (int i = 0; i < count; i++) {

            blockCipher.decryptBlock(data, i * blockSize, res, i * blockSize);

            for (int j = 0; j < blockSize; j++) {
                res[i * blockSize + j] = (byte) ((res[i * blockSize + j] & 0xFF) ^ (r[j] & 0xFF));
            }

            for (int j = 0; j < blockSize; j++) {
                r[j] = data[i * blockSize + j];
            }
        }
    }
}
