package im.actor.messenger.core.actors.encryption;

/**
 * Created by ex3ndr on 14.09.14.
 */
public class RsaResult {

    private final RsaPart[] myParts;
    private final RsaPart[] foreignParts;

    public RsaResult(RsaPart[] myParts, RsaPart[] foreignParts) {
        this.myParts = myParts;
        this.foreignParts = foreignParts;
    }

    public RsaPart[] getMyParts() {
        return myParts;
    }

    public RsaPart[] getForeignParts() {
        return foreignParts;
    }

    public static class RsaPart {
        private final long keyHash;
        private final boolean isSuccessful;
        private final byte[] encryptedData;

        public RsaPart(byte[] encryptedData, long keyHash) {
            this.encryptedData = encryptedData;
            this.keyHash = keyHash;
            this.isSuccessful = true;
        }

        public RsaPart(long keyHash) {
            this.keyHash = keyHash;
            this.isSuccessful = false;
            this.encryptedData = null;
        }

        public boolean isSuccessful() {
            return isSuccessful;
        }

        public long getKeyHash() {
            return keyHash;
        }

        public byte[] getEncryptedData() {
            return encryptedData;
        }
    }
}
