package im.actor.core.modules.calls;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiCallMember;
import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.ResponseGetCallInfo;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallMemberState;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Consumer;

import static im.actor.core.modules.internal.messages.entity.EntityConverter.convert;

public class CallSlaveActor extends AbsCallActor {

    private static final String TAG = "PeerSlaveCallActor";

    private final ActorRef callManager;
    private final long callId;

    private Peer peer;
    private CallVM callVM;

    private boolean isConnected;
    private boolean isAnswered;
    private boolean isRejected;

    public CallSlaveActor(long callId, ModuleContext context) {
        super(true, context);

        getSelfSettings().setIsPreConnectionEnabled(true);

        this.callId = callId;
        this.callManager = context.getCallsModule().getCallManager();
        this.isAnswered = false;
        this.isConnected = false;
    }

    @Override
    public void preStart() {
        super.preStart();

        api(new RequestGetCallInfo(callId)).then(new Consumer<ResponseGetCallInfo>() {
            @Override
            public void apply(final ResponseGetCallInfo responseGetCallInfo) {
                peer = convert(responseGetCallInfo.getPeer());
                startSignaling(responseGetCallInfo.getEventBusId());
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                shutdown();
            }
        }).done(self());
    }

    @Override
    public void onSignalingStarted() {

        //
        // Creating Call VM
        //
        callVM = spawnNewVM(callId, peer, false, new ArrayList<CallMember>(), CallState.RINGING);

        //
        // Notify call manager to trigger ringing
        //
        callManager.send(new CallManagerActor.IncomingCallReady(callId), self());
    }

    @Override
    public void onFirstPeerStarted() {

        isConnected = true;

        if (isAnswered) {
            callVM.getState().change(CallState.IN_PROGRESS);
        }
    }

    @Override
    public void onMembersReceived(List<ApiCallMember> allMembers) {
        super.onMembersReceived(allMembers);

        ArrayList<CallMember> members = new ArrayList<>();
        for (ApiCallMember apiCallMember : allMembers) {
            if (getUser(apiCallMember.getUserId()) == null) {
                continue;
            }
            if (apiCallMember.getUserId() == myUid()) {
                continue;
            }
            CallMemberState state = CallMemberState.from(apiCallMember.getState());
            members.add(new CallMember(apiCallMember.getUserId(), state));
        }
        callVM.getMembers().change(members);
    }

    public void onAnswer() {
        if (!isAnswered && !isRejected) {
            isAnswered = true;

            sendAnswer();

            if (isConnected) {
                callVM.getState().change(CallState.IN_PROGRESS);
            } else {
                callVM.getState().change(CallState.CONNECTING);
            }
        }
    }

    public void onReject() {
        if (!isAnswered && !isRejected) {
            isRejected = true;

            sendReject();

            self().send(PoisonPill.INSTANCE);
        }
    }

    @Override
    public void postStop() {
        super.postStop();

        //
        // Update CallVM
        //
        if (callVM != null) {
            callVM.getState().change(CallState.ENDED);
        }

        //
        // Notify call manager about call end
        //
        callManager.send(new CallManagerActor.OnCallEnded(callId), self());
    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof DoAnswerCall) {
            onAnswer();
        } else if (message instanceof DoRejectCall) {
            onReject();
        } else {
            super.onReceive(message);
        }
    }

    public static class DoAnswerCall {

    }

    public static class DoRejectCall {

    }
}
