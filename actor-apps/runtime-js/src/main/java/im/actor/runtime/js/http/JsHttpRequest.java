/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.http;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

import im.actor.runtime.js.fs.JsBlob;

public class JsHttpRequest extends JavaScriptObject {

    public static native JsHttpRequest create()/*-{
        return new $wnd.XMLHttpRequest();
    }-*/;

    protected JsHttpRequest() {

    }

    public final native void open(String httpMethod, String url) /*-{
        this.open(httpMethod, url, true);
    }-*/;

    public final native void setRequestHeader(String header, String value) /*-{
        this.setRequestHeader(header, value);
    }-*/;

    public final native void setOnLoadHandler(JsHttpRequestHandler handler)/*-{
        var _this = this;
        this.onreadystatechange = $entry(function() {
            handler.@im.actor.runtime.js.http.JsHttpRequestHandler::onStateChanged(Lim/actor/runtime/js/http/JsHttpRequest;)(_this);
        });
    }-*/;

    public final native int getReadyState() /*-{
        return this.readyState;
    }-*/;

    public final native int getStatus() /*-{
        return this.status;
    }-*/;

    public final native void send(ArrayBuffer data)/*-{
        this.send(new Blob([data]));
    }-*/;

    public final native void send()/*-{
        this.send();
    }-*/;

    public final native void setResponseType(String responseType)/*-{
        this.responseType = responseType;
    }-*/;

    public final native String getResponseType()/*-{
        return this.responseType;
    }-*/;

    public final native String getResponseText()/*-{
        return this.responseText;
    }-*/;

    public final native JsBlob getResponseBlob()/*-{
        return this.response;
    }-*/;
}
