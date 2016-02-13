package im.actor.core.modules.calls;

import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.ResponseGetCallInfo;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.ModuleActor;
import im.actor.core.providers.CallsProvider;
import im.actor.core.util.RandomUtils;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.*;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Constructor;
import im.actor.runtime.function.Consumer;

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

    private CallsProvider provider;
    private HashSet<Long> handledCalls = new HashSet<>();
    private HashSet<Long> answeredCalls = new HashSet<>();

    private HashMap<Long, ActorRef> currentCalls = new HashMap<>();

    public CallManagerActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();
        provider = config().getCallsProvider();
    }


    //
    // Outgoing call
    //

    private void doCall(final Peer peer, final CommandCallback<Long> callback) {
        system().actorOf("actor/master/" + RandomUtils.nextRid(), new ActorCreator() {
            @Override
            public Actor create() {
                return new CallMasterActor(peer, context(), callback);
            }
        });
    }

    private void onCallCreated(long callId, ActorRef ref) {
        currentCalls.put(callId, ref);
        provider.onCallStart(callId);
    }


    //
    // Incoming call
    //

    private void onIncomingCall(long callId) {
        Log.d(TAG, "onIncomingCall (" + callId + ")");

        // Filter double updates about incoming call
        if (handledCalls.contains(callId)) {
            return;
        }
        handledCalls.add(callId);

        api(new RequestGetCallInfo(callId)).then(new Consumer<ResponseGetCallInfo>() {
            @Override
            public void apply(final ResponseGetCallInfo responseGetCallInfo) {
                system().actorOf("actor/slave", new ActorCreator() {
                    @Override
                    public Actor create() {
                        return new CallSlaveActor(responseGetCallInfo.getEventBusId(), context());
                    }
                });
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                // Just Ignore
            }
        }).done(self());
    }

    private void onIncomingCallHandled(long callId) {
        if (handledCalls.contains(callId) && !answeredCalls.contains(callId)) {
            // Kill Actor
        }
    }

    private void doAnswerCall(final long callId) {

    }


    //
    // Ending call
    //

    private void onCallEnded(long callId) {
        currentCalls.remove(callId);
    }

    private void doEndCall(long callId) {

    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnIncomingCall) {
            OnIncomingCall call = (OnIncomingCall) message;
            onIncomingCall(call.getCallId());
        } else if (message instanceof OnIncomingCallHandled) {
            OnIncomingCallHandled incomingCallHandled = (OnIncomingCallHandled) message;
            onIncomingCallHandled(incomingCallHandled.getCallId());
        } else if (message instanceof AnswerCall) {
            doAnswerCall(((AnswerCall) message).getCallId());
        } else if (message instanceof EndCall) {
            doEndCall(((EndCall) message).getCallId());
        } else if (message instanceof OnCallEnded) {
            onCallEnded(((OnCallEnded) message).getCallId());
        } else if (message instanceof DoCall) {
            DoCall doCall = (DoCall) message;
            doCall(doCall.getPeer(), doCall.getCallback());
        } else if (message instanceof OnCallCreated) {
            OnCallCreated callCreated = (OnCallCreated) message;
            onCallCreated(callCreated.getCallId(), sender());
        } else {
            super.onReceive(message);
        }
    }

    public static class OnIncomingCall {

        private long callId;

        public OnIncomingCall(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class OnIncomingCallHandled {

        private long callId;

        public OnIncomingCallHandled(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class OnCallEnded {
        private long callId;

        public OnCallEnded(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class AnswerCall {

        private long callId;

        public AnswerCall(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class EndCall {
        private long callId;

        public EndCall(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class DoCall {
        private Peer peer;
        private CommandCallback<Long> callback;

        public DoCall(Peer peer, CommandCallback<Long> callback) {
            this.peer = peer;
            this.callback = callback;
        }

        public CommandCallback<Long> getCallback() {
            return callback;
        }

        public Peer getPeer() {
            return peer;
        }
    }

    public static class OnCallCreated {

        private long callId;

        public OnCallCreated(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }
}
