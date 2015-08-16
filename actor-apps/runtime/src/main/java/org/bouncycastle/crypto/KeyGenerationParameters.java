package org.bouncycastle.crypto;

import im.actor.runtime.crypto.bouncycastle.RandomProvider;

/**
 * The base class for parameters to key generators.
 */
public class KeyGenerationParameters {
    private RandomProvider random;
    private int strength;

    /**
     * initialise the generator with a source of randomness
     * and a strength (in bits).
     *
     * @param random   the random byte source.
     * @param strength the size, in bits, of the keys we want to produce.
     */
    public KeyGenerationParameters(
            RandomProvider random,
            int strength) {
        this.random = random;
        this.strength = strength;
    }

    /**
     * return the random source associated with this
     * generator.
     *
     * @return the generators random source.
     */
    public RandomProvider getRandom() {
        return random;
    }

    /**
     * return the bit strength for keys produced by this generator,
     *
     * @return the strength of the keys this generator produces (in bits).
     */
    public int getStrength() {
        return strength;
    }
}
