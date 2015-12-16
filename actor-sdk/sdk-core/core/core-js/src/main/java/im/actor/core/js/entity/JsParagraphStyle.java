package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.core.api.ApiParagraphStyle;

public class JsParagraphStyle extends JavaScriptObject {

    public static JsParagraphStyle create(ApiParagraphStyle style) {
        if (style == null) {
            return JsParagraphStyle.create(false, null, null);
        }

        boolean show = style.showParagraph() != null ? style.showParagraph() : false;
        return JsParagraphStyle.create(show, JsColor.create(style.getParagraphColor()),
                JsColor.create(style.getBgColor()));
    }

    public static native JsParagraphStyle create(boolean showParagraph, JsColor color, JsColor bgColor)/*-{
        return { showParagraph: showParagraph, color: color, bgColor: bgColor };
    }-*/;

    protected JsParagraphStyle() {

    }
}
