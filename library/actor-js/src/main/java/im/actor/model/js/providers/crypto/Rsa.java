/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.crypto;

import im.actor.model.crypto.CryptoKeyPair;
import im.actor.model.util.Base64Utils;

public class Rsa {

    public static CryptoKeyPair generate1024RsaKey() {
        RsaKey rsaKey = generate1024RsaKeyJs();
        return new CryptoKeyPair(Base64Utils.fromBase64(rsaKey.getPublicKey()),
                Base64Utils.fromBase64(rsaKey.getPrivateKey()));
    }

    private static native RsaKey generate1024RsaKeyJs()/*-{
        var crypt = new $wnd.JSEncrypt({default_key_size: 1024});
        return {publicKey: crypt.getKey().getPublicBaseKeyB64(), privateKey: crypt.getKey().getPrivateBaseKeyB64()};
    }-*/;
}