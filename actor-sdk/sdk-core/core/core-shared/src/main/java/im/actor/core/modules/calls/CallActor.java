package im.actor.core.modules.calls;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiCallMember;
import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.ResponseGetCallInfo;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.AbsCallActor;
import im.actor.core.modules.calls.peers.PeerState;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallMemberState;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Consumer;

import static im.actor.core.modules.internal.messages.entity.EntityConverter.convert;

public class CallActor extends AbsCallActor {

    private static final String TAG = "CallActor";

    private final long callId;

    private Peer peer;
    private CallVM callVM;

    private boolean isConnected;
    private boolean isAnswered;
    private boolean isRejected;

    public CallActor(long callId, ModuleContext context) {
        super(context);
        this.callId = callId;
        this.isAnswered = false;
        this.isConnected = false;
    }

    @Override
    public void callPreStart() {
        api(new RequestGetCallInfo(callId)).then(new Consumer<ResponseGetCallInfo>() {
            @Override
            public void apply(final ResponseGetCallInfo responseGetCallInfo) {
                peer = convert(responseGetCallInfo.getPeer());
                callBus.joinBus(responseGetCallInfo.getEventBusId());
                callVM = callViewModels.spawnNewIncomingVM(callId, peer, CallState.RINGING);
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                self().send(PoisonPill.INSTANCE);
            }
        }).done(self());
    }

    @Override
    public void onBusStarted(String busId) {
        callManager.send(new CallManagerActor.IncomingCallReady(callId), self());
    }

    @Override
    public void onMembersChanged(List<ApiCallMember> members) {
        ArrayList<CallMember> vmMembers = new ArrayList<>();
        for (ApiCallMember callMember : members) {
            vmMembers.add(new CallMember(callMember.getUserId(),
                    CallMemberState.from(callMember.getState())));
        }
        callVM.getMembers().change(vmMembers);
    }

    @Override
    public void onPeerStateChanged(int uid, long deviceId, PeerState state) {
        if ((state == PeerState.CONNECTED || state == PeerState.ACTIVE) && !isConnected) {
            isConnected = true;
            if (isAnswered) {
                callVM.getState().change(CallState.IN_PROGRESS);
            }
        }
    }

    @Override
    public void onMuteChanged(boolean isMuted) {
        super.onMuteChanged(isMuted);
        callVM.getIsMuted().change(isMuted);
    }

    public void onAnswerCall() {
        if (!isAnswered && !isRejected) {
            isAnswered = true;
            callBus.answerCall();
            peerCall.onOwnStarted();
            if (isConnected) {
                callVM.getState().change(CallState.IN_PROGRESS);
            } else {
                callVM.getState().change(CallState.CONNECTING);
            }
        }
    }

    public void onRejectCall() {
        if (!isAnswered && !isRejected) {
            isRejected = true;
            callBus.rejectCall();
            peerCall.kill();
        }
    }

    @Override
    public void postStop() {
        super.postStop();
        if (callVM != null) {
            callVM.getState().change(CallState.ENDED);
        }
        callBus.kill();
        peerCall.kill();
    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof AnswerCall) {
            onAnswerCall();
        } else if (message instanceof RejectCall) {
            onRejectCall();
        } else {
            super.onReceive(message);
        }
    }

    public static class AnswerCall {

    }

    public static class RejectCall {

    }
}
