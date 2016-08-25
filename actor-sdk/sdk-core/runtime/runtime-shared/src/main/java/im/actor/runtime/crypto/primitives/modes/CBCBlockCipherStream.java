package im.actor.runtime.crypto.primitives.modes;

import im.actor.runtime.crypto.BlockCipher;

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class CBCBlockCipherStream implements BlockCipher {

    private byte[] iv;
    private BlockCipher baseCipher;
    private int blockSize;
    private byte[] workingSet;

    public CBCBlockCipherStream(byte[] iv, BlockCipher baseCipher) {
        if (iv.length != baseCipher.getBlockSize()) {
            throw new RuntimeException("Incorrect iv size");
        }

        this.baseCipher = baseCipher;
        this.blockSize = baseCipher.getBlockSize();
        this.iv = iv;
        this.workingSet = new byte[blockSize];
    }

    @Override
    public void encryptBlock(byte[] data, int offset, byte[] dest, int destOffset) {

//        // DATA = SRC_DATA ^ IV
//        for (int j = 0; j < blockSize; j++) {
//            workingSet[j] = (byte) ((data[offset + j] & 0xFF) ^ (iv[j] & 0xFF));
//        }

        // DEST = encrypt(DATA)
        // baseCipher.encryptBlock(workingSet, 0, dest, destOffset);
        baseCipher.encryptBlock(data, offset, dest, destOffset);

//        // IV = DEST
//        for (int j = 0; j < blockSize; j++) {
//            iv[j] = dest[destOffset + j];
//        }
    }

    @Override
    public void decryptBlock(byte[] data, int offset, byte[] dest, int destOffset) {

        // DEST = decrypt(DATA)
        baseCipher.decryptBlock(data, offset, dest, destOffset);

//        // DEST_RES = DEST ^ IV
//        for (int j = 0; j < blockSize; j++) {
//            dest[destOffset + j] = (byte) ((dest[destOffset + j] & 0xFF) ^ (iv[j] & 0xFF));
//        }

//        // IV = DATA
//        for (int j = 0; j < blockSize; j++) {
//            iv[j] = data[offset + j];
//        }
    }

    @Override
    public int getBlockSize() {
        return baseCipher.getBlockSize();
    }
}
