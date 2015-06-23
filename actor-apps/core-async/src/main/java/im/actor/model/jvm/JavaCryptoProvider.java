/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.jvm;

import im.actor.model.crypto.bouncycastle.BouncyCastleProvider;
import im.actor.model.crypto.bouncycastle.RandomProvider;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JavaCryptoProvider extends BouncyCastleProvider {
    public JavaCryptoProvider() {
        super(new JavaRandomProvider());
    }

    public JavaCryptoProvider(RandomProvider provider) {
        super(provider);
    }
}
