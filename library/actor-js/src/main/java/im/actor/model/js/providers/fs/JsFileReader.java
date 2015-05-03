/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.fs;

import com.google.gwt.core.client.JavaScriptObject;

public class JsFileReader extends JavaScriptObject {

    public native static JsFileReader create()/*-{
        return new FileReader();
    }-*/;

    protected JsFileReader() {

    }

    public native final void setOnLoaded(JsFileLoadedClosure closure)/*-{
        this.addEventListener("loadend", function() {
            console.log("loadend");
            closure.@im.actor.model.js.providers.fs.JsFileLoadedClosure::onLoaded()();
        });
    }-*/;

    public native final void readAsArrayBuffer(JsFileSlice slice)/*-{
        this.readAsArrayBuffer(slice);
    }-*/;
}