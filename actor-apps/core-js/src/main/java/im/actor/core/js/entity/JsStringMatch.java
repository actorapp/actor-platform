package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.core.util.StringMatch;

public class JsStringMatch extends JavaScriptObject {

    public static JsStringMatch create(StringMatch match) {
        return create(match.getStart(), match.getLength());
    }

    public static native JsStringMatch create(int start, int length)/*-{
        return {start: start, length: length };
    }-*/;

    protected JsStringMatch() {

    }
}
