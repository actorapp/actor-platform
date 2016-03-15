package im.actor.runtime.crypto.ratchet;

import im.actor.runtime.Crypto;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.crypto.Digest;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class RatchetKeySignature {
    public static byte[] hashForSignature(long keyId, String keyAlg, byte[] publicKey) {
        byte[] toSign;
        try {
            DataOutput dataOutput = new DataOutput();
            BserWriter writer = new BserWriter(dataOutput);
            writer.writeLong(1, keyId);
            writer.writeString(2, keyAlg);
            Digest sha256 = Crypto.createSHA256();
            sha256.update(publicKey, 0, publicKey.length);
            byte[] hash = new byte[32];
            sha256.doFinal(hash, 0);
            writer.writeBytes(3, hash);
            toSign = dataOutput.toByteArray();
        } catch (Exception e) {
            // Never happens
            return new byte[0];
        }
        return toSign;
    }
}
