/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.fs;

import com.google.gwt.core.client.JavaScriptObject;

public class JsFileReader extends JavaScriptObject {

    public native static JsFileReader create()/*-{
        return new FileReader();
    }-*/;

    protected JsFileReader() {

    }

    public native final void setOnLoaded(JsFileLoadedClosure closure)/*-{
        this.addEventListener("loadend", function(evt) {
            if (evt.target.readyState == FileReader.DONE) {
                console.log(evt.target.result);
                closure.@im.actor.runtime.js.fs.JsFileLoadedClosure::onLoaded(*)(evt.target.result);
            }
        });
    }-*/;

    public native final void readAsArrayBuffer(JsFileSlice slice)/*-{
        this.readAsArrayBuffer(slice);
    }-*/;
}