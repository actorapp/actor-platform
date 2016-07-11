package im.actor.runtime.android.crypto;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import im.actor.runtime.crypto.Digest;

public class AndroidSHA256 implements Digest {

    private MessageDigest messageDigest;

    public AndroidSHA256() {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            throw new RuntimeException(e1);// Unexpected
        }
    }

    @Override
    public void reset() {
        messageDigest.reset();
    }

    @Override
    public void update(byte[] src, int offset, int length) {
        messageDigest.update(src, offset, length);
    }

    @Override
    public void doFinal(byte[] dest, int destOffset) {
        try {
            messageDigest.digest(dest, destOffset, 32);
        } catch (DigestException e) {
            throw new RuntimeException(e);// Unexpected
        }
    }

    @Override
    public int getDigestSize() {
        return 32;
    }
}
