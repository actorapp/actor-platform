package im.actor.core.modules.calls;

import im.actor.core.api.rpc.RequestSubscribeToCalls;
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

    private long subscribeRequest = -1;

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
}
