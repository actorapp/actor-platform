/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.bouncycastle;

import java.math.BigInteger;

public interface RandomProvider {
    byte[] randomBytes(int length);

    int randomInt(int maxValue);

    void nextBytes(byte[] data);

    BigInteger generateBigInteger(int numBits);

    BigInteger generateBigInteger(int numBits, int certanity);
}
