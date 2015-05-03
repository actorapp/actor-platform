/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.fs;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by ex3ndr on 03.05.15.
 */
public class JsFile extends JavaScriptObject {
    protected JsFile() {

    }

    public final native int getSize()/*-{
        return this.size;
    }-*/;

    public final native JsFileSlice slice(int startByte, int endByte)/*-{
        return this.slice(startByte, endByte);
    }-*/;
}
