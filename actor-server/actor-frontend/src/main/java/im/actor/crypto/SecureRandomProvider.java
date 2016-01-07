package im.actor.crypto;

/**
 * Secure Random provider
 */
public interface SecureRandomProvider {
    /**
     * Securely generate random bytes
     *
     * @param buffer destination buffer
     * @param offset offset in buffer
     * @param length count of random bytes
     */
    void nextBytes(byte[] buffer, int offset, int length);

    /**
     * Next Random integer from [0,maxInt)
     *
     * @param maxInt maximum value (exclusive)
     * @return random integer
     */
    int nextInt(int maxInt);
}
