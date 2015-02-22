package im.actor.gwt.app.ui;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.model.entity.Peer;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JsPeer extends JavaScriptObject {

    public static JsPeer create(Peer peer) {
        switch (peer.getPeerType()) {
            default:
            case PRIVATE:
                return create(0, peer.getPeerId());
            case GROUP:
                return create(1, peer.getPeerId());
        }
    }

    public static native JsPeer create(int peerType, int peerId)/*-{
        return {peerType: peerType, peerId: peerId, key:peerType+':'+peerId};
    }-*/;

    protected JsPeer() {
    }

    public final native int getPeerType()/*-{ return this.peerType; }-*/;

    public final native int getPeerId()/*-{ return this.peerId; }-*/;

    public final Peer convert() {
        switch (getPeerType()) {
            case 0:
            default:
                return Peer.user(getPeerId());
            case 1:
                return Peer.group(getPeerId());
        }
    }
}
