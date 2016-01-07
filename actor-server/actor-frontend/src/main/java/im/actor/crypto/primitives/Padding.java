package im.actor.crypto.primitives;

/**
 * Padding of data
 *
 * @author Steve Kite (steve@actor.im)
 */
public interface Padding {
    /**
     * Writing padding bytes
     *
     * @param src    destination data
     * @param offset offset in src
     * @param length length of padding
     */
    void padding(byte[] src, int offset, int length);

    /**
     * Validating padding bytes
     *
     * @param src    destination data
     * @param offset offset in src
     * @param length length of padding
     */
    boolean validate(byte[] src, int offset, int length);
}