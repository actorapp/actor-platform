/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.runtime.crypto.bouncycastle;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;

import im.actor.core.crypto.RsaEncryptCipher;

public class BcRsaEncryptCipher implements RsaEncryptCipher {

    private AsymmetricBlockCipher cipher;
    private RandomProvider random;

    public BcRsaEncryptCipher(RandomProvider random, byte[] publicKey) {
        this.random = random;
        try {
            im.actor.core.runtime.crypto.encoding.X509RsaPublicKey key = new im.actor.core.runtime.crypto.encoding.X509RsaPublicKey(publicKey);
            RSAKeyParameters param = new RSAKeyParameters(false, key.getModulus(), key.getExponent());
            cipher = new OAEPEncoding(new RSAEngine(), new SHA1Digest());
            cipher.init(true, new ParametersWithRandom(param, random));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized byte[] encrypt(byte[] sourceData) {
        if (cipher == null) {
            return null;
        }
        try {
            return cipher.processBlock(sourceData, 0, sourceData.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}