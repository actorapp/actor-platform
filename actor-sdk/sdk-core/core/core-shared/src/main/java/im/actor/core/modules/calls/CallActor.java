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
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Consumer;

import static im.actor.core.modules.internal.messages.entity.EntityConverter.convert;

public class CallActor extends AbsCallActor {

    private final boolean isMaster;
    private long callId;
    private Peer peer;
    private CallVM callVM;
    private CommandCallback<Long> callback;

    private boolean isActive;
    private boolean isAnswered;
    private boolean isRejected;

    public CallActor(long callId, ModuleContext context) {
        super(context);
        this.isMaster = false;
        this.callId = callId;
        this.isAnswered = false;
        this.isActive = false;
    }

    public CallActor(Peer peer, CommandCallback<Long> callback, ModuleContext context) {
        super(context);
        this.isMaster = true;
        this.callback = callback;
        this.peer = peer;
        this.isAnswered = true;
        this.isActive = false;
    }

    @Override
    public void preStart() {
        super.preStart();
        if (!isMaster) {
            api(new RequestGetCallInfo(callId)).then(new Consumer<ResponseGetCallInfo>() {
                @Override
                public void apply(final ResponseGetCallInfo responseGetCallInfo) {
                    peer = convert(responseGetCallInfo.getPeer());
                    callBus.startSlaveBus(responseGetCallInfo.getEventBusId());
                    callVM = callViewModels.spawnNewIncomingVM(callId, peer, CallState.RINGING);
                }
            }).failure(new Consumer<Exception>() {
                @Override
                public void apply(Exception e) {
                    self().send(PoisonPill.INSTANCE);
                }
            }).done(self());
        } else {
            callBus.startMaster();
        }
    }

    @Override
    public void onBusStarted(String busId) {
        if (isMaster) {
            api(new RequestDoCall(buidOutPeer(peer), busId)).then(new Consumer<ResponseDoCall>() {
                @Override
                public void apply(ResponseDoCall responseDoCall) {
                    callId = responseDoCall.getCallId();
                    callVM = callViewModels.spawnNewOutgoingVM(responseDoCall.getCallId(), peer);
                    callManager.send(new CallManagerActor.DoCallComplete(responseDoCall.getCallId()), self());
                    callback.onResult(responseDoCall.getCallId());
                    callback = null;
                }
            }).failure(new Consumer<Exception>() {
                @Override
                public void apply(Exception e) {
                    callback.onError(e);
                    callback = null;
                }
            }).done(self());
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
