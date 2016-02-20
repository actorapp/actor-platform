package im.actor.core.modules.calls;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.entity.Group;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.PeerCallActor;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallMemberState;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;

public class AbsCallActor extends PeerCallActor {

    private final HashMap<Long, CallVM> callModels;
    private CallVM callVM;

    public AbsCallActor(boolean isSlaveMode, ModuleContext context) {
        super(isSlaveMode, context);

        callModels = context().getCallsModule().getCallModels();
    }

    //
    // Call Model helpers
    //
    public CallVM spawnNewVM(long callId, Peer peer, boolean isOutgoing, ArrayList<CallMember> members, CallState callState) {
        CallVM callVM = new CallVM(callId, peer, isOutgoing, members, callState);
        callVM.getIsMuted().change(isMuted());
        synchronized (callModels) {
            callModels.put(callId, callVM);
        }
        this.callVM = callVM;
        return callVM;
    }

    public CallVM spanNewOutgoingVM(long callId, Peer peer) {
        ArrayList<CallMember> members = new ArrayList<>();
        if (peer.getPeerType() == PeerType.PRIVATE ||
                peer.getPeerType() == PeerType.PRIVATE_ENCRYPTED) {
            members.add(new CallMember(peer.getPeerId(), CallMemberState.RINGING));
        } else if (peer.getPeerType() == PeerType.GROUP) {
            Group g = getGroup(peer.getPeerId());
            for (GroupMember gm : g.getMembers()) {
                if (gm.getUid() != myUid()) {
                    members.add(new CallMember(gm.getUid(), CallMemberState.RINGING));
                }
            }
        }
        return spawnNewVM(callId, peer, true, members, CallState.RINGING);
    }

    @Override
    public void onMute(boolean isMuted) {
        super.onMute(isMuted);
        if (callVM != null) {
            callVM.getIsMuted().change(isMuted);
        }
    }
}
