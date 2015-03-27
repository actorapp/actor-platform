package im.actor.model;

import im.actor.model.crypto.AesCipher;
import im.actor.model.crypto.CryptoKeyPair;
import im.actor.model.crypto.RsaCipher;
import im.actor.model.crypto.RsaEncryptCipher;

/**
 * Created by ex3ndr on 25.02.15.
 */
public interface CryptoProvider {

    public CryptoKeyPair generateRSA1024KeyPair();

    public RsaEncryptCipher createRSAOAEPSHA1Cipher(byte[] publicKey);

    public RsaCipher createRSAOAEPSHA1Cipher(byte[] publicKey, byte[] privateKey);

    public AesCipher createAESCBCPKS7Cipher(byte[] key, byte[] iv);

    public byte[] MD5(byte[] data);

    public byte[] SHA256(byte[] data);

    public byte[] SHA512(byte[] data);

    public byte[] randomBytes(int length);

    public int randomInt(int maxValue);
}