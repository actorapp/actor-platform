/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.fs;

public class JsFile extends JsBlob {
    protected JsFile() {

    }

    public final native String getName()/*-{
        return this.name;
    }-*/;

}
