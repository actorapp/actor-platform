package im.actor.core.modules.calls.peers;

import java.util.List;

import im.actor.core.api.ApiCallMember;
import im.actor.core.modules.calls.peers.PeerCallInt;
import im.actor.core.modules.calls.peers.PeerState;

public interface CallBusCallback {

    void onBusCreated(PeerCallInt peerCallInt);

    void onBusStarted(String busId);

    void onMembersChanged(List<ApiCallMember> members);

    void onPeerStateChanged(int uid, long deviceId, PeerState state);
}
