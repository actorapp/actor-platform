/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsPeerInfo extends JavaScriptObject {

    public native static JsPeerInfo create(JsPeer peer, String title, String avatar, String placeholder)/*-{
        return {peer: peer, title: title, avatar: avatar, placeholder: placeholder}
    }-*/;

    protected JsPeerInfo() {

    }
}