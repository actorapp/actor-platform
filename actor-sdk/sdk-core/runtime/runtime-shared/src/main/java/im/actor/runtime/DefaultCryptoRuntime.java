package im.actor.runtime;

import im.actor.runtime.crypto.BlockCipher;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.primitives.aes.AESFastEngine;
import im.actor.runtime.crypto.primitives.digest.SHA256;

public abstract class DefaultCryptoRuntime implements CryptoRuntime {

    @Override
    public Digest SHA256() {
        return new SHA256();
    }

    @Override
    public BlockCipher AES128(byte[] key) {
        return new AESFastEngine(key);
    }
}
