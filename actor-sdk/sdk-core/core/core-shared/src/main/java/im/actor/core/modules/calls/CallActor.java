package im.actor.core.modules.calls;

import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.RequestJoinCall;
import im.actor.core.api.rpc.RequestRejectCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.api.rpc.ResponseGetCallInfo;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.AbsCallActor;
import im.actor.core.modules.calls.peers.CallBusActor;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.power.WakeLock;

import static im.actor.core.entity.EntityConverter.convert;

public class CallActor extends AbsCallActor {

    private final boolean isMaster;
    private final WakeLock wakeLock;
    private long callId;
    private Peer peer;
    private CallVM callVM;
    private CommandCallback<Long> callback;

    private boolean isActive;
    private boolean isAnswered;
    private boolean isRejected;

    public CallActor(long callId, WakeLock wakeLock, ModuleContext context) {
        super(context);
        this.wakeLock = wakeLock;
        this.isMaster = false;
        this.callId = callId;
        this.isAnswered = false;
        this.isActive = false;
    }

    public CallActor(Peer peer, CommandCallback<Long> callback, WakeLock wakeLock, ModuleContext context) {
        super(context);
        this.wakeLock = wakeLock;
        this.isMaster = true;
        this.callback = callback;
        this.peer = peer;
        this.isAnswered = true;
        this.isActive = false;
    }

    @Override
    public void preStart() {
        super.preStart();
        if (isMaster) {
            api(new RequestDoCall(buidOutPeer(peer), CallBusActor.TIMEOUT)).then(responseDoCall -> {
                callId = responseDoCall.getCallId();
                callBus.joinMasterBus(responseDoCall.getEventBusId(), responseDoCall.getDeviceId());
                callBus.startOwn();
                callVM = callViewModels.spawnNewOutgoingVM(responseDoCall.getCallId(), peer);
            }).failure(e -> self().send(PoisonPill.INSTANCE));
        } else {
            api(new RequestGetCallInfo(callId)).then(responseGetCallInfo -> {
                peer = convert(responseGetCallInfo.getPeer());
                callBus.joinBus(responseGetCallInfo.getEventBusId());
                callVM = callViewModels.spawnNewIncomingVM(callId, peer, CallState.RINGING);
            }).failure(e -> self().send(PoisonPill.INSTANCE));
        }
    }

    @Override
    public void onBusStarted(String busId) {
        if (isMaster) {
            callManager.send(new CallManagerActor.DoCallComplete(callId), self());

            callback.onResult(callId);
            callback = null;
        } else {
            callManager.send(new CallManagerActor.IncomingCallReady(callId), self());
        }
    }

    @Override
    public void onCallConnected() {
        // callVM.getState().change()
    }

    @Override
    public void onCallEnabled() {
        isActive = true;
        if (isAnswered) {
            callVM.getState().change(CallState.IN_PROGRESS);
            callVM.setCallStart(im.actor.runtime.Runtime.getCurrentTime());
        }
        if (isMaster) {
            callManager.send(new CallManagerActor.OnCallAnswered(callId), self());
        }
    }

    @Override
    public void onBusStopped() {
        self().send(PoisonPill.INSTANCE);
    }


    @Override
    public void onMuteChanged(boolean isMuted) {
        super.onMuteChanged(isMuted);
        callVM.getIsMuted().change(isMuted);
    }

    public void onAnswerCall() {
        if (!isAnswered && !isRejected) {
            isAnswered = true;
            callBus.startOwn();
            request(new RequestJoinCall(callId));

            if (isActive) {
                callVM.getState().change(CallState.IN_PROGRESS);
                callVM.setCallStart(im.actor.runtime.Runtime.getCurrentTime());
            } else {
                callVM.getState().change(CallState.CONNECTING);
            }
        }
    }

    public void onRejectCall() {
        if (!isAnswered && !isRejected) {
            isRejected = true;
            request(new RequestRejectCall(callId));
            self().send(PoisonPill.INSTANCE);
        }
    }

    @Override
    public void postStop() {
        super.postStop();
        if (callVM != null) {
            callVM.getState().change(CallState.ENDED);
            callVM.setCallEnd(im.actor.runtime.Runtime.getCurrentTime());
        }
        callBus.kill();
        if (callId != 0) {
            callManager.send(new CallManagerActor.OnCallEnded(callId), self());
        }
        wakeLock.releaseLock();
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
