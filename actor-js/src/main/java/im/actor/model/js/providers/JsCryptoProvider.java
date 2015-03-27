package im.actor.model.js.providers;

import im.actor.model.crypto.CryptoKeyPair;
import im.actor.model.crypto.bouncycastle.BouncyCastleProvider;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsCryptoProvider extends BouncyCastleProvider {
    public JsCryptoProvider() {
        super(new JsRandomProvider());
    }

    @Override
    public CryptoKeyPair generateRSA1024KeyPair() {
        // return super.generateRSA1024KeyPair();
        return new CryptoKeyPair(new byte[64], new byte[64]);
    }
}
