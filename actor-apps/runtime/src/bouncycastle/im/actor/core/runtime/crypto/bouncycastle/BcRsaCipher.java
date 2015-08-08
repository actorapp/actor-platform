/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.runtime.crypto.bouncycastle;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;

import im.actor.core.crypto.RsaCipher;

public class BcRsaCipher extends BcRsaEncryptCipher implements RsaCipher {
    private AsymmetricBlockCipher cipher;

    public BcRsaCipher(RandomProvider random, byte[] publicKey, byte[] privateKey) {
        super(random, publicKey);

        try {
            im.actor.core.runtime.crypto.encoding.PKS8RsaPrivateKey pks8RsaPrivateKey = new im.actor.core.runtime.crypto.encoding.PKS8RsaPrivateKey(privateKey);
            AsymmetricKeyParameter keyParameter = new RSAKeyParameters(true, pks8RsaPrivateKey.getModulus(),
                    pks8RsaPrivateKey.getExponent());
            cipher = new OAEPEncoding(new RSAEngine(), new SHA1Digest());
            cipher.init(false, new ParametersWithRandom(keyParameter, random));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized byte[] decrypt(byte[] sourceData) {
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
