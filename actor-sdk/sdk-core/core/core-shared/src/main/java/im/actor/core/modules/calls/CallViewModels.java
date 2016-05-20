package im.actor.core.modules.calls;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.entity.Group;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallMemberState;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;

public class CallViewModels {

    private final HashMap<Long, CallVM> callModels;
    private final ModuleContext context;

    public CallViewModels(ModuleContext context) {
        this.context = context;
        this.callModels = new HashMap<>();
    }

    public synchronized CallVM getCall(long id) {
        return callModels.get(id);
    }

    public synchronized CallVM spawnNewVM(long callId, Peer peer, boolean isOutgoing, ArrayList<CallMember> members, CallState callState) {
        CallVM callVM = new CallVM(callId, peer, isOutgoing, members, callState);
        synchronized (callModels) {
            callModels.put(callId, callVM);
        }
        return callVM;
    }

    public synchronized CallVM spawnNewIncomingVM(long callId, Peer peer, CallState callState) {
        CallVM callVM = new CallVM(callId, peer, false, new ArrayList<>(), callState);
        synchronized (callModels) {
            callModels.put(callId, callVM);
        }
        return callVM;
    }

    public synchronized CallVM spawnNewOutgoingVM(long callId, Peer peer) {
        ArrayList<CallMember> members = new ArrayList<>();
        if (peer.getPeerType() == PeerType.PRIVATE ||
                peer.getPeerType() == PeerType.PRIVATE_ENCRYPTED) {
            members.add(new CallMember(peer.getPeerId(), CallMemberState.RINGING));
        } else if (peer.getPeerType() == PeerType.GROUP) {
            Group g = context.getGroupsModule().getGroups().getValue(peer.getPeerId());
            for (GroupMember gm : g.getMembers()) {
                if (gm.getUid() != context.getAuthModule().myUid()) {
                    members.add(new CallMember(gm.getUid(), CallMemberState.RINGING));
                }
            }
        }
        return spawnNewVM(callId, peer, true, members, CallState.RINGING);
    }
}
