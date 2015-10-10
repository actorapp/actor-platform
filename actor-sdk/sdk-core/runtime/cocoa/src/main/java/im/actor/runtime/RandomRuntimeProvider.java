package im.actor.runtime;

import java.math.BigInteger;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class RandomRuntimeProvider implements RandomRuntime {
    @Override
    public byte[] randomBytes(int length) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public int randomInt(int maxValue) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public void nextBytes(byte[] data) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public BigInteger generateBigInteger(int numBits) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public BigInteger generateBigInteger(int numBits, int certanity) {
        throw new RuntimeException("Dumb");
    }
}
