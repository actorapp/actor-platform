package im.actor.core.modules.calls.bus;

import im.actor.core.modules.calls.peers.PeerCallInt;

public interface CallBusCallback {
    void onBusCreated(PeerCallInt peerCallInt);

    void onBusStarted(String busId);
}
