package im.actor.sdk.controllers;

import im.actor.core.entity.Peer;

public interface PeerListFragmentDelegate {
    void onPeerClicked(Peer peer);

    boolean onPeerLongClicked(Peer peer);
}
