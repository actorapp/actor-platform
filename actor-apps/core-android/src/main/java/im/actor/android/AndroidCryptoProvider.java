/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android;

import im.actor.model.jvm.JavaCryptoProvider;

public class AndroidCryptoProvider extends JavaCryptoProvider {
    public AndroidCryptoProvider() {
        super(new AndroidRandomProvider());
    }
}
