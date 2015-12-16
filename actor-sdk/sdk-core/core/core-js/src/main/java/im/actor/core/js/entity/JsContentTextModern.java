package im.actor.core.js.entity;

import com.google.gwt.core.client.JsArray;

public class JsContentTextModern extends JsContent {

    public native static JsContentText create(String text, JsParagraphStyle paragraphStyle,
                                              JsArray<JsAttach> attaches)/*-{
        return {content: "text_modern", text: text, paragraphStyle: paragraphStyle, attaches: attaches };
    }-*/;

    protected JsContentTextModern() {

    }
}
