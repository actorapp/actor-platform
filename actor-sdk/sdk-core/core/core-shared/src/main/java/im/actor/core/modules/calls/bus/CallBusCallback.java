package im.actor.core.modules.calls.bus;

import java.util.List;

import im.actor.core.api.ApiCallMember;
import im.actor.core.modules.calls.peers.PeerCallInt;

public interface CallBusCallback {

    void onBusCreated(PeerCallInt peerCallInt);

    void onBusStarted(String busId);

    void onMembersChanged(List<ApiCallMember> members);
}
