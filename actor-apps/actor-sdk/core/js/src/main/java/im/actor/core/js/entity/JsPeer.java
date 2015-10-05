/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.core.entity.Peer;

public class JsPeer extends JavaScriptObject {

    public static JsPeer create(Peer peer) {
        switch (peer.getPeerType()) {
            default:
            case PRIVATE:
                return create("user", peer.getPeerId(), "u" + peer.getPeerId());
            case GROUP:
                return create("group", peer.getPeerId(), "g" + peer.getPeerId());
        }
    }

    public static native JsPeer create(String peerType, int peerId, String peerKey)/*-{
        return {type: peerType, id: peerId, key:peerKey};
    }-*/;

    protected JsPeer() {
    }

    public final native String getPeerType()/*-{ return this.type; }-*/;

    public final native int getPeerId()/*-{ return this.id; }-*/;

    public final Peer convert() {
        if (getPeerType().equals("user")) {
            return Peer.user(getPeerId());
        } else {
            return Peer.group(getPeerId());
        }
    }


}
