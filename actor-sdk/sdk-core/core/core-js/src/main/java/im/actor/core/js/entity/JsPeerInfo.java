/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsPeerInfo extends JavaScriptObject {

    public native static JsPeerInfo create(JsPeer peer, String title, String userName,  String avatar, String placeholder,
                                           boolean isVerified)/*-{
        return {peer: peer, title: title, userName: userName, avatar: avatar, placeholder: placeholder, isVerified: isVerified};
    }-*/;

    protected JsPeerInfo() {

    }

    public final native JsPeer getPeer()/*-{
        return this.peer;
    }-*/;

    public final native String getTitle()/*-{
        return this.title;
    }-*/;

    public final native String getAvatar()/*-{
        return this.avatar;
    }-*/;
}