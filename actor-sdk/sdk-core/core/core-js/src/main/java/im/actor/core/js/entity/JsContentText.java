/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

public class JsContentText extends JsContent {
    public native static JsContentText create(String text)/*-{
        return {content: "text", text: text};
    }-*/;

    protected JsContentText(){

    }
}
