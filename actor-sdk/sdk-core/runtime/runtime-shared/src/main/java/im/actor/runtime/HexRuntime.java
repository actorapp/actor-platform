package im.actor.runtime;

public interface HexRuntime {
    byte[] fromHex(String hex);

    byte[] fromHexReverse(String hex);

    String toHex(byte[] raw);
}
