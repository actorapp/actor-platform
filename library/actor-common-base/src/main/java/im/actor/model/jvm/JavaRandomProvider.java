/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.jvm;

import im.actor.model.crypto.bouncycastle.RandomProvider;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JavaRandomProvider implements RandomProvider {
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
