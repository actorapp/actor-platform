/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.fs;

/**
 * Created by ex3ndr on 03.05.15.
 */
public class JsFile extends JsBlob {
    protected JsFile() {

    }

    public final native String getName()/*-{
        return this.name;
    }-*/;

}
