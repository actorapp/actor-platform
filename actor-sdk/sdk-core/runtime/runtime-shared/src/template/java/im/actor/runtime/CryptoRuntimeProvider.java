package im.actor.runtime;

public class CryptoRuntimeProvider implements CryptoRuntime {

    @Override
    public byte[] MD5(byte[] data) {
        throw new RuntimeException("Dumb");
    }

}
