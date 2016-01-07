package im.actor.crypto.primitives;

/**
 * Message Digest interface
 *
 * @author Steve Kite (steve@actor.im)
 */
public interface Digest {

    /**
     * Reset Digest state
     */
    void reset();

    /**
     * Update digest with data
     *
     * @param src    data
     * @param offset offset
     * @param length length
     */
    void update(byte[] src, int offset, int length);

    /**
     * Calculate digest value
     *
     * @param dest       destination array
     * @param destOffset offset
     */
    void doFinal(byte[] dest, int destOffset);

    /**
     * Return size of a digest
     *
     * @return digest size in bytes
     */
    int getDigestSize();
}
