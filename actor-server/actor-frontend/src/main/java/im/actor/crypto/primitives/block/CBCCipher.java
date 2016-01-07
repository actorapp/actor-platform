package im.actor.crypto.primitives.block;

import im.actor.crypto.primitives.BlockCipher;

/**
 * CBC Cipher implementation
 * by Steve Kite (steve@actor.im)
 */
public class CBCCipher {
    private final BlockCipher blockCipher;
    private final int blockSize;

    /**
     * Constructing block cipher
     *
     * @param blockCipher Underlying block cipher
     */
    public CBCCipher(BlockCipher blockCipher) {
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
    public byte[] encrypt(byte[] iv, byte[] data) {
        if (data.length % blockSize != 0) {
            throw new RuntimeException("Incorrect data size");
        }
        if (iv.length != blockSize) {
            throw new RuntimeException("Incorrect iv size");
        }

        byte[] currentBlock = new byte[blockSize];
        byte[] res = new byte[data.length];

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

        return res;
    }

    /**
     * Decrypting data
     *
     * @param iv   initialization vector
     * @param data data for decryption
     * @return decrypted data
     */
    public byte[] decrypt(byte[] iv, byte[] data) {
        if (data.length % blockSize != 0) {
            throw new RuntimeException("Incorrect data size");
        }
        if (iv.length != blockSize) {
            throw new RuntimeException("Incorrect iv size");
        }

        byte[] res = new byte[data.length];

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

        return res;
    }
}
