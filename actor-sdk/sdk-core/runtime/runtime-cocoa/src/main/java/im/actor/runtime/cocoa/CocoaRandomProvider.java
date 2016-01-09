package im.actor.runtime.cocoa;

import java.math.BigInteger;
import java.security.SecureRandom;

import im.actor.runtime.crypto.bouncycastle.RandomProvider;

public class CocoaRandomProvider implements RandomProvider {

    private SecureRandom secureRandom = new SecureRandom();

    @Override
    public synchronized byte[] randomBytes(int length) {
        byte[] res = new byte[length];
        secureRandom.nextBytes(res);
        return res;
    }

    @Override
    public synchronized int randomInt(int maxValue) {
        return secureRandom.nextInt(maxValue);
    }

    @Override
    public synchronized void nextBytes(byte[] data) {
        secureRandom.nextBytes(data);
    }

    @Override
    public synchronized BigInteger generateBigInteger(int numBits) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public synchronized BigInteger generateBigInteger(int numBits, int certanity) {
        throw new RuntimeException("Not supported");
    }
}
