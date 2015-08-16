/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

public class JsContentUnsupported extends JsContent {
    public native static JsContentUnsupported create()/*-{
        return {content:"unsupported"};
    }-*/;

    protected JsContentUnsupported() {

    }
}
