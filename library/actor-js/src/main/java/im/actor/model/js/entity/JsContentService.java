/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class JsContentService extends JsContent {
    public native static JsContentService create(String text)/*-{
        return {content: "service", text: text};
    }-*/;

    protected JsContentService() {

    }
}
