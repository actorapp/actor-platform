package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsAttach extends JavaScriptObject {

    public static native JsAttach create(String title, String titleUrl, String text,
                                         JsParagraphStyle paragraphStyle,
                                         JsArray<JsAttachField> fields)/*-{
        return {title: title, titleUrl: titleUrl, text: text, paragraphStyle: paragraphStyle, fields: fields};
    }-*/;

    protected JsAttach() {

    }
}
