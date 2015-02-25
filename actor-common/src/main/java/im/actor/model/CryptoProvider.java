package im.actor.model;

/**
 * Created by ex3ndr on 25.02.15.
 */
public interface CryptoProvider {
    public byte[] SHA256(byte[] data);
}
