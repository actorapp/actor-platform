/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsGroupMember extends JavaScriptObject {
    public static native JsGroupMember create(JsPeerInfo peerInfo, boolean isAdmin, boolean canKick)/*-{
        return {peerInfo: peerInfo, isAdmin: isAdmin, canKick: canKick};
    }-*/;

    protected JsGroupMember() {

    }

    public final native JsPeerInfo getPeerInfo()/*-{
        return this.peerInfo;
    }-*/;
}