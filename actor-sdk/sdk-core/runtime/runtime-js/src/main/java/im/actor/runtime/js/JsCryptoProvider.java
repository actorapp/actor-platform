/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import im.actor.runtime.CryptoRuntime;
import im.actor.runtime.DefaultCryptoRuntime;
import im.actor.runtime.crypto.primitives.kuznechik.KuznechikFastEngine;

public class JsCryptoProvider extends DefaultCryptoRuntime {

    public JsCryptoProvider() {
        KuznechikFastEngine.initCalc();
    }

    @Override
    public void waitForCryptoLoaded() {

    }
}
