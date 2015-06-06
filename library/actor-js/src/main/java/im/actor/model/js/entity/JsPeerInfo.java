/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsPeerInfo extends JavaScriptObject {

    public native static JsPeerInfo create(JsPeer peer, String title, String avatar, String placeholder)/*-{
        return {peer: peer, title: title, avatar: avatar, placeholder: placeholder}
    }-*/;

    protected JsPeerInfo() {

    }
}