package im.actor.core.modules.calls;

import im.actor.core.api.rpc.RequestCallInProgress;
import im.actor.core.api.rpc.RequestSendCallSignal;
import im.actor.core.api.rpc.RequestSubscribeToCalls;
import im.actor.core.entity.signals.AbsSignal;
import im.actor.core.events.NewSessionCreated;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.function.Constructor;

public class CallManagerActor extends ModuleActor {

    public static Constructor<CallManagerActor> CONSTRUCTOR(final ModuleContext context) {
        return new Constructor<CallManagerActor>() {
            @Override
            public CallManagerActor create() {
                return new CallManagerActor(context);
            }
        };
    }

    private static final String TAG = "CallManagerActor";
    private static final int IN_PROGRESS_TIMEOUT = 15000;

    private long subscribeRequest = -1;
    private long progressRequest = -1;

    private long runningCallId = -1;

    public CallManagerActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();
        subscribeForCalls();
        subscribe(NewSessionCreated.EVENT);
    }

    private void onIncomingCall(long callId, int uid) {
        Log.d(TAG, "onIncomingCall (" + callId + ", " + uid + ")");
        if (runningCallId != -1) {
            return;
        }
        runningCallId = callId;
        config().getWebRTCProvider().onIncomingCall();
    }

    private void onAnswerCall() {
        Log.d(TAG, "onAnswerCall");
        if (runningCallId == -1) {
            return;
        }

        progressRequest = request(new RequestCallInProgress(runningCallId, IN_PROGRESS_TIMEOUT));
    }

    private void onSignaling(long callId, byte[] message) {
        Log.d(TAG, "onSignaling (" + callId + ")");
        if (runningCallId != callId) {
            return;
        }

        AbsSignal signal = AbsSignal.fromBytes(message);
        if (signal != null) {
            config().getWebRTCProvider().onSignalingReceived(signal);
        }
    }

    private void onSendSignal(AbsSignal signal) {
        Log.d(TAG, "onSendSignal: " + signal);
        if (runningCallId == -1) {
            return;
        }

        request(new RequestSendCallSignal(runningCallId, signal.toByteArray()));
    }

    private void subscribeForCalls() {
        if (subscribeRequest != -1) {
            context().getActorApi().cancelRequest(subscribeRequest);
            subscribeRequest = -1;
        }
        subscribeRequest = request(new RequestSubscribeToCalls());
    }

    @Override
    public void onBusEvent(Event event) {
        if (NewSessionCreated.EVENT.equals(event.getType())) {
            subscribeForCalls();
        } else {
            super.onBusEvent(event);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnIncomingCall) {
            OnIncomingCall call = (OnIncomingCall) message;
            onIncomingCall(call.getCallId(), call.getUid());
        } else if (message instanceof OnSignaling) {
            OnSignaling signaling = (OnSignaling) message;
            onSignaling(signaling.getCallId(), signaling.getMessage());
        } else if (message instanceof AnswerCall) {
            onAnswerCall();
        } else if (message instanceof SendSignaling) {
            onSendSignal(((SendSignaling) message).getSignal());
        } else {
            super.onReceive(message);
        }
    }

    public static class OnIncomingCall {

        private long callId;
        private int uid;

        public OnIncomingCall(long callId, int uid) {
            this.callId = callId;
            this.uid = uid;
        }

        public long getCallId() {
            return callId;
        }

        public int getUid() {
            return uid;
        }
    }

    public static class OnSignaling {
        private long callId;
        private byte[] message;

        public OnSignaling(long callId, byte[] message) {
            this.callId = callId;
            this.message = message;
        }

        public long getCallId() {
            return callId;
        }

        public byte[] getMessage() {
            return message;
        }
    }

    public static class AnswerCall {

    }

    public static class SendSignaling {

        private AbsSignal signal;

        public SendSignaling(AbsSignal signal) {
            this.signal = signal;
        }

        public AbsSignal getSignal() {
            return signal;
        }
    }
}
