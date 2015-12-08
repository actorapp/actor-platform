package im.actor.runtime;

public class CryptoRuntimeProvider implements CryptoRuntime {

    @Override
    public byte[] MD5(byte[] data) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public byte[] SHA256(byte[] data) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public byte[] SHA512(byte[] data) {
        throw new RuntimeException("Dumb");
    }
}
