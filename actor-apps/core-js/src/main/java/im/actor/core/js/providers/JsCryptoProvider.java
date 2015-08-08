/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers;

import im.actor.core.crypto.CryptoKeyPair;
import im.actor.core.crypto.bouncycastle.BouncyCastleProvider;
import im.actor.core.js.providers.crypto.Rsa;

public class JsCryptoProvider extends BouncyCastleProvider {
    public JsCryptoProvider() {
        super(new JsRandomProvider());
    }

    @Override
    public CryptoKeyPair generateRSA1024KeyPair() {
        return Rsa.generate1024RsaKey();
    }
}
