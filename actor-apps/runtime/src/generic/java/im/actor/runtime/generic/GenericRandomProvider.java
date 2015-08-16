package im.actor.runtime.generic;

import java.math.BigInteger;
import java.security.SecureRandom;

import im.actor.runtime.RandomRuntime;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class GenericRandomProvider implements RandomRuntime {

    private final SecureRandom random = new SecureRandom();

    @Override
    public byte[] randomBytes(int length) {
        byte[] res = new byte[length];
        synchronized (random) {
            random.nextBytes(res);
        }
        return res;
    }

    @Override
    public int randomInt(int maxValue) {
        synchronized (random) {
            return random.nextInt(maxValue);
        }
    }

    @Override
    public void nextBytes(byte[] data) {
        synchronized (random) {
            random.nextBytes(data);
        }
    }

    @Override
    public BigInteger generateBigInteger(int numBits) {
        return new BigInteger(numBits, random);
    }

    @Override
    public BigInteger generateBigInteger(int numBits, int certanity) {
        return new BigInteger(numBits, certanity, random);
    }
}
