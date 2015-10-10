/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.crypto;

import com.google.gwt.core.client.JavaScriptObject;

public class RsaKey extends JavaScriptObject {
    protected RsaKey() {

    }

    public final native String getPrivateKey()/*-{ return this.privateKey; }-*/;

    public final native String getPublicKey()/*-{ return this.publicKey; }-*/;
}
