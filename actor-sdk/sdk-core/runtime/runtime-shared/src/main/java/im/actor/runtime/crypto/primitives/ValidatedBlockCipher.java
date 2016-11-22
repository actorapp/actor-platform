package im.actor.runtime.crypto.primitives;

import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.BlockCipher;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ValidatedBlockCipher implements BlockCipher {

    private BlockCipher referenceImplementation;
    private BlockCipher modifiedImplementation;

    public ValidatedBlockCipher(BlockCipher referenceImplementation, BlockCipher modifiedImplementation) {
        this.referenceImplementation = referenceImplementation;
        this.modifiedImplementation = modifiedImplementation;
    }

    @Override
    public void encryptBlock(byte[] data, int offset, byte[] dest, int destOffset) {
        byte[] dest1 = new byte[getBlockSize()];
        byte[] dest2 = new byte[getBlockSize()];

        referenceImplementation.encryptBlock(data, offset, dest1, 0);
        modifiedImplementation.encryptBlock(data, offset, dest2, 0);

        for (int i = 0; i < dest1.length; i++) {
            if (dest1[i] != dest2[i]) {

                throw new RuntimeException("Mismatched output at " + i + ": "
                        + Crypto.hex(dest1) + " " + Crypto.hex(dest2));
            }
        }

        referenceImplementation.encryptBlock(data, offset, dest, destOffset);
    }

    @Override
    public void decryptBlock(byte[] data, int offset, byte[] dest, int destOffset) {
        referenceImplementation.decryptBlock(data, offset, dest, destOffset);
    }

    @Override
    public int getBlockSize() {
        if (referenceImplementation.getBlockSize() != modifiedImplementation.getBlockSize()) {
            throw new RuntimeException();
        }
        return modifiedImplementation.getBlockSize();
    }
}
