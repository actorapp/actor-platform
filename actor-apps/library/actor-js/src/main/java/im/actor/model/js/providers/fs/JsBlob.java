/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.fs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

import im.actor.model.js.utils.Conversion;

/**
 * Created by ex3ndr on 09.05.15.
 */
public class JsBlob extends JavaScriptObject {

    public static JsBlob createBlob(byte[] data) {
        return createBlob(Conversion.convertBytes(data));
    }

    public static native JsBlob createBlob(ArrayBuffer buffer)/*-{
        return Blob(new Blob([buffer]));
    }-*/;

    protected JsBlob() {

    }

    public final native JsFileSlice slice(int startByte, int endByte)/*-{
        return this.slice(startByte, endByte);
    }-*/;

    public final native int getSize()/*-{
        return this.size;
    }-*/;

    public final native String getMimeType()/*-{
        return this.type;
    }-*/;
}
