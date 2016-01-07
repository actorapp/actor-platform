package im.actor.crypto.primitives;

public interface BlockCipher {

    int getBlockSize();

    void encryptBlock(byte[] data, int offset, byte[] dest, int destOffset);

    void decryptBlock(byte[] data, int offset, byte[] dest, int destOffset);
}