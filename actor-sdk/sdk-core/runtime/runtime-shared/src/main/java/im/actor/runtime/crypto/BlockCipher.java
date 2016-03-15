package im.actor.runtime.crypto;

import com.google.j2objc.annotations.ObjectiveCName;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

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
    @ObjectiveCName("encryptBlock:withOffset:toDest:withOffset:")
    void encryptBlock(byte[] data, int offset, byte[] dest, int destOffset);

    /**
     * Decrypting block
     *
     * @param data       cipher-text data
     * @param offset     offset for data
     * @param dest       destination buffer
     * @param destOffset destination offset
     */
    @ObjectiveCName("decryptBlock:withOffset:toDest:withOffset:")
    void decryptBlock(byte[] data, int offset, byte[] dest, int destOffset);

    /**
     * Getting cipher's block size
     *
     * @return block size in bytes
     */
    @ObjectiveCName("getBlockSize")
    int getBlockSize();
}