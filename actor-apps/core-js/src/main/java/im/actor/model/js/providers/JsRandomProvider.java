/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import im.actor.model.crypto.bouncycastle.RandomProvider;

import java.math.BigInteger;
import java.util.Random;

public class JsRandomProvider implements RandomProvider {
    private Random random = new Random();

    @Override
    public byte[] randomBytes(int length) {
        byte[] res = new byte[length];
        random.nextBytes(res);
        return res;
    }

    @Override
    public int randomInt(int maxValue) {
        return random.nextInt(maxValue);
    }

    @Override
    public void nextBytes(byte[] data) {
        random.nextBytes(data);
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
