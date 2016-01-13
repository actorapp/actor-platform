package im.actor.core.modules.internal.calls;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.CallsModule;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.runtime.actors.messages.PoisonPill;

public class CallActor extends ModuleActor {

    private int timeout = 0;
    private boolean alive = false;
    private long callId;
    private CallsModule.CallCallback callback;

    public CallActor(long callId, CallsModule.CallCallback callback, ModuleContext context) {
        super(context);
        this.callId = callId;
        this.callback = callback;
    }

    @Override
    public void preStart() {
        super.preStart();
        self().send(new CheckAlive(), CallsModule.CALL_TIMEOUT);
        self().send(new SendCallInProgress());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof CallInProgress) {
            onCallInProgress(((CallInProgress) message).getTimeout());
        } else if (message instanceof CheckAlive) {
            checkAlive();
        } else if (message instanceof SendCallInProgress) {
            sendCallInProgress();
        } else if (message instanceof EndCall) {
            onEndCall();
        } else if (message instanceof Signal) {
            onSignal(((Signal) message).getData());
        }
    }

    public void onSignal(byte[] data) {
        callback.onSignal(data);
    }

    public void onEndCall() {
        callback.onCallEnd();
        context().getCallsModule().onCallEnded(callId);
        self().send(PoisonPill.INSTANCE);
    }

    public void checkAlive() {
        if (alive) {
            alive = false;
            self().send(new CheckAlive(), timeout);
        } else {
            context().getCallsModule().endCall(callId);
            self().send(PoisonPill.INSTANCE);
        }
    }

    private void sendCallInProgress() {
        context().getCallsModule().callInProgress(callId);
        self().send(new SendCallInProgress(), CallsModule.CALL_TIMEOUT);

    }

    private void onCallInProgress(int timeout) {
        alive = true;
        this.timeout = timeout;
    }

    public static class EndCall {

    }

    public static class Signal {
        byte[] data;

        public Signal(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class CallInProgress {

        int timeout;

        public CallInProgress(int timeout) {
            this.timeout = timeout;
        }

        public int getTimeout() {
            return timeout;
        }

    }

    private static class CheckAlive {

    }

    private static class SendCallInProgress {

    }
}
