/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.http;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

public class JsXmlHttpRequest extends JavaScriptObject {

    public static native JsXmlHttpRequest create()/*-{
        return new $wnd.XMLHttpRequest();
    }-*/;

    protected JsXmlHttpRequest() {

    }

    public final native void open(String httpMethod, String url) /*-{
        this.open(httpMethod, url, true);
    }-*/;

    public final native void setRequestHeader(String header, String value) /*-{
        this.setRequestHeader(header, value);
    }-*/;

    public final native void setOnLoadHandler(JsXmlHttpRequestHandler handler)/*-{
        var _this = this;
        this.onreadystatechange = $entry(function() {
            handler.@im.actor.model.js.providers.http.JsXmlHttpRequestHandler::onStateChanged(Lim/actor/model/js/providers/http/JsXmlHttpRequest;)(_this);
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
}
