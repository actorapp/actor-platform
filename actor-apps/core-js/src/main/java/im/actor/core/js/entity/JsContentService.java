/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

public class JsContentService extends JsContent {
    public native static JsContentService create(String text)/*-{
        return {content: "service", text: text};
    }-*/;

    protected JsContentService() {

    }
}
