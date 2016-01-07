package im.actor.crypto.primitives.hmac;

import im.actor.crypto.primitives.ByteStrings;
import im.actor.crypto.primitives.Hash;

import static im.actor.crypto.primitives.ByteStrings.merge;
import static im.actor.crypto.primitives.ByteStrings.substring;

public class HMAC {
    public static void hmac(byte[] secret, byte[] message, int offset, int length, byte[] dest, int destOffset, Hash hash) {
        byte[] fixedSecret = new byte[hash.getHashSize()];
        if (secret.length > hash.getHashSize()) {
            hash.hash(secret, 0, secret.length, fixedSecret, 0);
        } else if (secret.length < hash.getHashSize()) {
            ByteStrings.write(fixedSecret, 0, secret, 0, secret.length);
            for (int i = secret.length; i < fixedSecret.length; i++) {
                fixedSecret[i] = 0;
            }
        } else {
            fixedSecret = secret;
        }

        // Paddings
        byte[] outerKeyPad = new byte[hash.getHashSize()];
        byte[] innerKeyPad = new byte[hash.getHashSize()];
        for (int i = 0; i < outerKeyPad.length; i++) {
            outerKeyPad[i] = (byte) (0x5c ^ fixedSecret[i]);
            innerKeyPad[i] = (byte) (0x36 ^ fixedSecret[i]);
        }

        // Inner hash
        // hash(i_key_pad ∥ message)
        byte[] innnerHash = new byte[hash.getHashSize()];
        hash.hash(merge(innerKeyPad, substring(message, offset, length)), 0, outerKeyPad.length, innnerHash, 0);

        // Outer hash
        // hash(o_key_pad ∥ hash(i_key_pad ∥ message))
        hash.hash(merge(outerKeyPad, innnerHash), 0, hash.getHashSize() * 2, dest, destOffset);
    }
}
