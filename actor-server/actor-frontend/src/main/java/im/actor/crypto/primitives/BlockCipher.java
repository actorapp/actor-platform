package im.actor.crypto.primitives;

/**
 * Block-based cipher
 *
 * @author Steve Kite (steve@actor.im)
 */
public interface BlockCipher {

    /**
     * Encrypting block
     *
     * @param data       plain-text data
     * @param offset     offset for data
     * @param dest       destination buffer
     * @param destOffset destination offset
     */
    void encryptBlock(byte[] data, int offset, byte[] dest, int destOffset);

    /**
     * Decrypting block
     *
     * @param data       cipher-text data
     * @param offset     offset for data
     * @param dest       destination buffer
     * @param destOffset destination offset
     */
    void decryptBlock(byte[] data, int offset, byte[] dest, int destOffset);

    /**
     * Getting cipher's block size
     *
     * @return block size in bytes
     */
    int getBlockSize();
}