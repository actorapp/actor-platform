package im.actor.sdk.controllers.root;

import im.actor.core.entity.Peer;

public interface PeerSelectedCallback {
    void onPeerClick(Peer peer);

    boolean onPeerLongClick(Peer peer);
}
