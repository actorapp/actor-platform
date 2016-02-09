package im.actor.crypto.primitives.hmac;

import im.actor.crypto.primitives.util.ByteStrings;
import im.actor.crypto.primitives.Digest;

import static im.actor.crypto.primitives.util.ByteStrings.merge;
import static im.actor.crypto.primitives.util.ByteStrings.substring;

public class HMAC implements Digest {

    private Digest digest;
    private final byte[] secret;
    private final byte[] outerKeyPad;
    private final byte[] innerKeyPad;

    public HMAC(byte[] secret, Digest digest) {

        this.digest = digest;

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
        this.secret = fixedSecret;

        outerKeyPad = new byte[digest.getDigestSize()];
        innerKeyPad = new byte[digest.getDigestSize()];
        for (int i = 0; i < outerKeyPad.length; i++) {
            outerKeyPad[i] = (byte) (0x5c ^ (this.secret[i] & 0xFF));
            innerKeyPad[i] = (byte) (0x36 ^ (this.secret[i] & 0xFF));
        }
    }

    @Override
    public void reset() {
        digest.reset();
        digest.update(innerKeyPad, 0, innerKeyPad.length);
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        digest.update(src, offset, length);
    }

    @Override
    public void doFinal(byte[] dest, int destOffset) {
        byte[] innnerHash = new byte[digest.getDigestSize()];
        digest.doFinal(innnerHash, 0);
        digest.reset();
        digest.update(outerKeyPad, 0, outerKeyPad.length);
        digest.update(innnerHash, 0, innnerHash.length);
        digest.doFinal(dest, destOffset);

        reset();
    }

    @Override
    public int getDigestSize() {
        return digest.getDigestSize();
    }
}
