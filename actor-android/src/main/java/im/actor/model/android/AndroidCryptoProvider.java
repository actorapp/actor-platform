package im.actor.model.android;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import im.actor.model.CryptoProvider;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class AndroidCryptoProvider implements CryptoProvider {
    @Override
    public byte[] SHA256(byte[] data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No SHA-256 available");
        }
    }
}
