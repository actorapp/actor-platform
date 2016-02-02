package im.actor.runtime;

public class CryptoRuntimeProvider implements CryptoRuntime {

    @Override
    public byte[] MD5(byte[] data) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public byte[] fromHex(String hex) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public byte[] fromHexReverse(String hex) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public String toHex(byte[] raw) {
        throw new RuntimeException("Dumb");
    }

}
