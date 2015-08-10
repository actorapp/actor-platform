package org.bouncycastle.crypto.params;

import im.actor.runtime.crypto.bouncycastle.RandomProvider;
import org.bouncycastle.crypto.CipherParameters;

public class ParametersWithRandom
        implements CipherParameters {
    private RandomProvider random;
    private CipherParameters parameters;

    public ParametersWithRandom(
            CipherParameters parameters,
            RandomProvider random) {
        this.random = random;
        this.parameters = parameters;
    }

    public RandomProvider getRandom() {
        return random;
    }

    public CipherParameters getParameters() {
        return parameters;
    }
}
