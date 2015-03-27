package im.actor.model.crypto.bouncycastle;

import java.math.BigInteger;
import java.util.Random;

/**
 * Created by ex3ndr on 27.03.15.
 */
public interface RandomProvider {
    public byte[] randomBytes(int length);

    public int randomInt(int maxValue);

    public void nextBytes(byte[] data);

    public BigInteger generateBigInteger(int numBits);

    public BigInteger generateBigInteger(int numBits, int certanity);
}
