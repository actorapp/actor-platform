package im.actor.model.js.providers.crypto;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class RsaKey extends JavaScriptObject {
    protected RsaKey() {

    }

    public final native String getPrivateKey()/*-{ return this.privateKey; }-*/;

    public final native String getPublicKey()/*-{ return this.publicKey; }-*/;
}
