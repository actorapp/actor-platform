package im.actor.model.crypto;

/**
 * Created by ex3ndr on 07.03.15.
 */
public interface AesCipher {
    public byte[] encrypt(byte[] source);

    public byte[] decrypt(byte[] source);
}
