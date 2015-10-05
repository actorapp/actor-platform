/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.crypto.bouncycastle;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import im.actor.runtime.crypto.AesCipher;

public class BcAesCipher implements AesCipher {

    private byte[] key;
    private byte[] iv;

    private CipherParameters params;
    private BufferedBlockCipher encryptionCipher;
    private BufferedBlockCipher decryptionCipher;

    public BcAesCipher(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
        this.params = new ParametersWithIV(new KeyParameter(key), iv);
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getIv() {
        return iv;
    }

    @Override
    public synchronized byte[] encrypt(byte[] source) {
        if (encryptionCipher == null) {
            encryptionCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()),
                    new PKCS7Padding());
            encryptionCipher.init(true, params);
        }

        encryptionCipher.reset();
        byte[] buf = new byte[encryptionCipher.getOutputSize(source.length)];
        int len = encryptionCipher.processBytes(source, 0, source.length, buf, 0);
        try {
            len += encryptionCipher.doFinal(buf, len);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
            return null;
        }

        byte[] res = new byte[len];
        System.arraycopy(buf, 0, res, 0, len);
        return res;
    }

    @Override
    public synchronized byte[] decrypt(byte[] source) {
        if (decryptionCipher == null) {
            decryptionCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()),
                    new PKCS7Padding());
            decryptionCipher.init(false, params);
        }

        decryptionCipher.reset();
        byte[] buf = new byte[decryptionCipher.getOutputSize(source.length)];
        int len = decryptionCipher.processBytes(source, 0, source.length, buf, 0);
        try {
            len += decryptionCipher.doFinal(buf, len);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
            return null;
        }

        byte[] res = new byte[len];
        System.arraycopy(buf, 0, res, 0, len);
        return res;
    }
}
