package im.actor.runtime.crypto.ratchet;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class RatchetPublicKey {

    private byte[] key;

    public RatchetPublicKey(byte[] key) {
        if (key == null) {
            throw new IllegalArgumentException("Key can't be null");
        }
        if (key.length != 32) {
            throw new IllegalArgumentException("Key MUST be 32 bytes length");
        }
        this.key = key;
    }

    public byte[] getKey() {
        return key;
    }

    public boolean areSame(byte[] key) {
        if (key == null) {
            throw new IllegalArgumentException("Key can't be null");
        }
        if (key.length != 32) {
            throw new IllegalArgumentException("Key MUST be 32 bytes length");
        }
        for (int i = 0; i < 32; i++) {
            if (key[i] != this.key[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean isBigger(byte[] key) {
        if (key == null) {
            throw new IllegalArgumentException("Key can't be null");
        }
        if (key.length != 32) {
            throw new IllegalArgumentException("Key MUST be 32 bytes length");
        }
        for (int i = 0; i < 32; i++) {
            int foreign = key[i] & 0xFF;
            int own = this.key[i] & 0xFF;
            if (foreign < own) {
                return true;
            } else if (foreign > own) {
                return false;
            }
        }
        return false;
    }
}