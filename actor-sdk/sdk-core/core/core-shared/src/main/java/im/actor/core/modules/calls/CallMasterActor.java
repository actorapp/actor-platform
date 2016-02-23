package im.actor.core.modules.calls;

import java.util.HashSet;

import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.AbsCallActor;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.Log;
import im.actor.runtime.function.Consumer;

public class CallMasterActor extends AbsCallActor {

    private final Peer peer;
    private long callId;
    private CallVM callVM;
    private CommandCallback<Long> callback;

    public CallMasterActor(Peer peer, ModuleContext context, CommandCallback<Long> callback) {
        super(context);
        this.peer = peer;
        this.callback = callback;
    }

    @Override
    public void callPreStart() {
        callBus.startMaster();
        peerCall.onOwnStarted();
    }

    @Override
    public void onBusStarted(String busId) {
        api(new RequestDoCall(buidOutPeer(peer), busId)).then(new Consumer<ResponseDoCall>() {
            @Override
            public void apply(ResponseDoCall responseDoCall) {

                //
                // Initialization of Call State
                //
                // TODO: Possible race conditions when members changed during call initiation
                // Need to return explicit callers in response

                if (peer.getPeerType() == PeerType.GROUP) {
                    for (GroupMember gm : getGroup(peer.getPeerId()).getMembers()) {
                        if (gm.getUid() != myUid()) {
                            // state.addMember(gm.getUid(), MasterCallMemberState.RINGING);
                        }
                    }
                } else if (peer.getPeerType() == PeerType.PRIVATE) {
                    // state.addMember(peer.getPeerId(), MasterCallMemberState.RINGING);
                } else {
                    throw new RuntimeException("Unsupported Peer Type group");
                }

                //
                // Initialization of CallVM
                //
                callId = responseDoCall.getCallId();
                callVM = callViewModels.spawnNewOutgoingVM(responseDoCall.getCallId(), peer);
                // callVM.getIsMuted().change(isMuted());

                //
                // Notifying about successful call creation
                //
                callManager.send(new CallManagerActor.DoCallComplete(responseDoCall.getCallId()), self());
                callback.onResult(responseDoCall.getCallId());
                callback = null;
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                callback.onError(e);
                callback = null;
                dispose();
            }
        }).done(self());
    }

    @Override
    public void onAnswered(int uid, long deviceId) {
        Log.d("CallMasterActor", "On Answered: " + uid + ", device: " + deviceId);
        peerCall.onTheirStarted(deviceId);
    }

    @Override
    public void onPeerConnected(int uid, long deviceId) {
        Log.d("CallMasterActor", "On Peer Connected: " + uid + ", device: " + deviceId);
        peerCall.onOfferNeeded(deviceId);
    }

    @Override
    public void postStop() {
        super.postStop();
        if (callback != null) {
            callback.onError(new RuntimeException());
            callback = null;
        }
    }
}
