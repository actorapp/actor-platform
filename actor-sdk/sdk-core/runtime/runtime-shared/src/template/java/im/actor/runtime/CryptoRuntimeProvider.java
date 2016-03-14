package im.actor.runtime;

import im.actor.runtime.crypto.Digest;

public class CryptoRuntimeProvider implements CryptoRuntime {

    @Override
    public Digest SHA256() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public void waitForCryptoLoaded() {
        throw new RuntimeException("Dumb");
    }
}
