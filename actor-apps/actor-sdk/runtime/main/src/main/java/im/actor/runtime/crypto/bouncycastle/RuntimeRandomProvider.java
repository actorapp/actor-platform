package im.actor.runtime.crypto.bouncycastle;

import java.math.BigInteger;

import im.actor.runtime.Crypto;

public class RuntimeRandomProvider implements RandomProvider {

    public static final RuntimeRandomProvider INSTANCE = new RuntimeRandomProvider();

    @Override
    public byte[] randomBytes(int length) {
        return Crypto.randomBytes(length);
    }

    @Override
    public int randomInt(int maxValue) {
        return Crypto.randomInt(maxValue);
    }

    @Override
    public void nextBytes(byte[] data) {
        Crypto.nextBytes(data);
    }

    @Override
    public BigInteger generateBigInteger(int numBits) {
        return Crypto.generateBigInteger(numBits);
    }

    @Override
    public BigInteger generateBigInteger(int numBits, int certanity) {
        return Crypto.generateBigInteger(numBits, certanity);
    }
}
