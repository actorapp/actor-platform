package im.actor.runtime.crypto.ratchet;

import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.crypto.primitives.digest.SHA256;

public class RatchetKeySignature {
    public static byte[] hashForSignature(long keyId, String keyAlg, byte[] publicKey) {
        byte[] toSign;
        try {
            DataOutput dataOutput = new DataOutput();
            BserWriter writer = new BserWriter(dataOutput);
            writer.writeLong(1, keyId);
            writer.writeString(2, keyAlg);
            SHA256 sha256 = new SHA256();
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
