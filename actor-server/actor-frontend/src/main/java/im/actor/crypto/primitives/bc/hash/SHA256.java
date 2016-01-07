package im.actor.crypto.primitives.bc.hash;

import im.actor.crypto.primitives.Hash;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 implements Hash {

    private final MessageDigest md;

    public SHA256() {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("SHA-256 is not available");
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
