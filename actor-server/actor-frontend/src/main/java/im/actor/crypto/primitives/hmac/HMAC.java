package im.actor.crypto.primitives.hmac;

import im.actor.crypto.primitives.util.ByteStrings;
import im.actor.crypto.primitives.Digest;

import static im.actor.crypto.primitives.util.ByteStrings.merge;
import static im.actor.crypto.primitives.util.ByteStrings.substring;

public class HMAC {

    private Digest digest;

    public HMAC(Digest digest) {
        this.digest = digest;
    }

    public void calculate(byte[] secret, byte[] message, int offset, int length, byte[] dest, int destOffset) {
        byte[] fixedSecret = new byte[digest.getDigestSize()];
        if (secret.length > digest.getDigestSize()) {
            digest.reset();
            digest.update(secret, 0, secret.length);
            digest.doFinal(fixedSecret, 0);
        } else if (secret.length < digest.getDigestSize()) {
            ByteStrings.write(fixedSecret, 0, secret, 0, secret.length);
            for (int i = secret.length; i < fixedSecret.length; i++) {
                fixedSecret[i] = 0;
            }
        } else {
            fixedSecret = secret;
        }

        // Paddings
        byte[] outerKeyPad = new byte[digest.getDigestSize()];
        byte[] innerKeyPad = new byte[digest.getDigestSize()];
        for (int i = 0; i < outerKeyPad.length; i++) {
            outerKeyPad[i] = (byte) (0x5c ^ fixedSecret[i]);
            innerKeyPad[i] = (byte) (0x36 ^ fixedSecret[i]);
        }

        // Inner digest
        // digest(i_key_pad ∥ message)
        byte[] innnerHash = new byte[digest.getDigestSize()];
        digest.reset();
        digest.update(innerKeyPad, 0, innerKeyPad.length);
        digest.update(message, offset, length);
        digest.doFinal(innnerHash, 0);

        // Outer digest
        // digest(o_key_pad ∥ digest(i_key_pad ∥ message))
        digest.reset();
        digest.update(outerKeyPad, 0, outerKeyPad.length);
        digest.update(innnerHash, 0, innnerHash.length);
        digest.doFinal(dest, destOffset);
    }
}
