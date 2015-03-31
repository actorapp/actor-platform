package im.actor.android;

import im.actor.model.jvm.JavaCryptoProvider;

/**
 * Created by ex3ndr on 29.03.15.
 */
public class AndroidCryptoProvider extends JavaCryptoProvider {
    public AndroidCryptoProvider() {
        super(new AndroidRandomProvider());
    }
}
