package im.actor.model.js.providers;

import im.actor.model.crypto.CryptoKeyPair;
import im.actor.model.crypto.bouncycastle.BouncyCastleProvider;
import im.actor.model.js.providers.crypto.Rsa;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsCryptoProvider extends BouncyCastleProvider {
    public JsCryptoProvider() {
        super(new JsRandomProvider());
    }

    @Override
    public CryptoKeyPair generateRSA1024KeyPair() {
        return Rsa.generate1024RsaKey();
    }
}
