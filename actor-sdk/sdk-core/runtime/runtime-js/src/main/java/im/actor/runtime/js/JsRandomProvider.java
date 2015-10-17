/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import java.math.BigInteger;
import java.util.Random;

import im.actor.runtime.RandomRuntime;

public class JsRandomProvider implements RandomRuntime {
    
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
