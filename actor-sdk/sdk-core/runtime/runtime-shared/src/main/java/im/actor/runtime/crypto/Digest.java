package im.actor.runtime.crypto;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Message Digest interface
 *
 * @author Steve Kite (steve@actor.im)
 */
public interface Digest {

    /**
     * Reset Digest state
     */
    @ObjectiveCName("reset")
    void reset();

    /**
     * Update digest with data
     *
     * @param src    data
     * @param offset offset
     * @param length length
     */
    @ObjectiveCName("update:withOffset:withLength:")
    void update(byte[] src, int offset, int length);

    /**
     * Calculate digest value
     *
     * @param dest       destination array
     * @param destOffset offset
     */
    @ObjectiveCName("doFinal:withOffset:")
    void doFinal(byte[] dest, int destOffset);

    /**
     * Return size of a digest
     *
     * @return digest size in bytes
     */
    @ObjectiveCName("getDigestSize")
    int getDigestSize();
}
