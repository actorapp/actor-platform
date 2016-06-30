package im.actor.runtime.crypto.primitives;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

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