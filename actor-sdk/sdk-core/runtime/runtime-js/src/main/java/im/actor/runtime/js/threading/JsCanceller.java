package im.actor.runtime.js.threading;

import com.google.gwt.core.client.JavaScriptObject;

public final class JsCanceller extends JavaScriptObject {

    protected JsCanceller() {

    }

    public native void cancel()/*-{
        clearTimeout(this);
    }-*/;
}
