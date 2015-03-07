package im.actor.model.crypto;

/**
 * Created by ex3ndr on 07.03.15.
 */
public interface RsaEncryptCipher {
    public byte[] encrypt(byte[] sourceData);
}
