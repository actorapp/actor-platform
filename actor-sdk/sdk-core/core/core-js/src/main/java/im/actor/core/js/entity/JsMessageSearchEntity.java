package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsMessageSearchEntity extends JavaScriptObject {

    public static native JsMessageSearchEntity create(String rid, JsPeerInfo sender, String date,
                                                      JsContent content)/*-{
        return { rid: rid, sender: sender, date: date, content: content };
    }-*/;

    protected JsMessageSearchEntity() {

    }
}
