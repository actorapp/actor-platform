package im.actor.runtime;

import im.actor.runtime.crypto.AesCipher;
import im.actor.runtime.crypto.CryptoKeyPair;
import im.actor.runtime.crypto.RsaCipher;
import im.actor.runtime.crypto.RsaEncryptCipher;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class CryptoRuntimeProvider implements CryptoRuntime {
    @Override
    public CryptoKeyPair generateRSA1024KeyPair() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public RsaEncryptCipher createRSAOAEPSHA1Cipher(byte[] publicKey) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public RsaCipher createRSAOAEPSHA1Cipher(byte[] publicKey, byte[] privateKey) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public AesCipher createAESCBCPKS7Cipher(byte[] key, byte[] iv) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public byte[] MD5(byte[] data) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public byte[] SHA256(byte[] data) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public byte[] SHA512(byte[] data) {
        throw new RuntimeException("Dumb");
    }
}
