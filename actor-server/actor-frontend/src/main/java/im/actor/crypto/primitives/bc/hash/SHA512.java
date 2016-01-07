package im.actor.crypto.primitives.bc.hash;

import im.actor.crypto.primitives.Hash;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA512 implements Hash {

    private final MessageDigest md;

    public SHA512() {
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("SHA-512 is not available");
        }
    }

    @Override
    public void hash(byte[] src, int offset, int length, byte[] dest, int destOffset) {
        md.reset();
        md.update(src, offset, length);
        try {
            md.digest(dest, destOffset, md.getDigestLength());
        } catch (DigestException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getHashSize() {
        return md.getDigestLength();
    }
}
