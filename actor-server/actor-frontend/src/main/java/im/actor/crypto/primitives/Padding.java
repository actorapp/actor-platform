package im.actor.crypto.primitives;

public interface Padding {
    void padding(byte[] src, int offset, int length);
}
