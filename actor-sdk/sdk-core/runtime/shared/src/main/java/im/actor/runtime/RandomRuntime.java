package im.actor.runtime;

import java.math.BigInteger;

public interface RandomRuntime {
    byte[] randomBytes(int length);

    int randomInt(int maxValue);

    void nextBytes(byte[] data);

    BigInteger generateBigInteger(int numBits);

    BigInteger generateBigInteger(int numBits, int certanity);
}
