package im.actor.crypto.primitives;

public interface Hash {

    int getHashSize();

    void hash(byte[] src, int offset, int length, byte[] dest, int destOffset);
}
