/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;


import im.actor.runtime.crypto.CryptoKeyPair;
import im.actor.runtime.crypto.bouncycastle.BouncyCastleRuntime;
import im.actor.runtime.js.crypto.Rsa;

public class JsCryptoProvider extends BouncyCastleRuntime {

    @Override
    public CryptoKeyPair generateRSA1024KeyPair() {
        return Rsa.generate1024RsaKey();
    }
}
