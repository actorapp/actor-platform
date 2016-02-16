package im.actor.core.modules.calls;

import java.util.HashMap;
import java.util.HashSet;

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

    private Long currentCall;
    private HashMap<Long, ActorRef> runningCalls = new HashMap<>();

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
        Log.d(TAG, "doCall (" + peer + ")");

        //
        // Stopping current call as we started new done
        //
        stopRunningCall();

        //
        // Spawning new Actor for call
        //
        system().actorOf("actor/master/" + RandomUtils.nextRid(), new ActorCreator() {
            @Override
            public Actor create() {
                return new CallMasterActor(peer, context(), callback);
            }
        });
    }

    private void onCallCreated(long callId, ActorRef ref) {

        //
        // Stopping current call some are started during call establishing
        //
        stopRunningCall();

        //
        // Saving Reference to call
        //
        runningCalls.put(callId, ref);

        //
        // Setting Current Call
        //
        currentCall = callId;

        //
        // Notify Provider about new current call
        //
        provider.onCallStart(callId);
    }


    //
    // Incoming call
    //

    private void onIncomingCall(final long callId) {
        Log.d(TAG, "onIncomingCall (" + callId + ")");

        //
        // Filter double updates about incoming call
        //
        if (handledCalls.contains(callId)) {
            return;
        }
        handledCalls.add(callId);

        //
        // Spawning new Actor for call
        //
        system().actorOf("actor/slave/" + RandomUtils.nextRid(), new ActorCreator() {
            @Override
            public Actor create() {
                return new CallSlaveActor(callId, context());
            }
        });
    }

    private void onIncomingCallReady(long callId, ActorRef ref) {

        //
        // Saving reference to incoming call
        //
        runningCalls.put(callId, ref);

        //
        // Change Current Call if there are no ongoing calls now
        //
        if (currentCall == null) {
            currentCall = callId;
            provider.onCallStart(callId);
        }
    }

    private void onIncomingCallHandled(long callId) {
        if (handledCalls.contains(callId) && !answeredCalls.contains(callId)) {
            // Kill Actor
        }
    }

    private void doAnswerCall(final long callId) {
        answeredCalls.add(callId);
    }


    //
    // Ending call
    //

    private void onCallEnded(long callId) {
        runningCalls.remove(callId);
    }

    private void doEndCall(long callId) {
        ActorRef currentCall = runningCalls.remove(callId);
        if (currentCall != null) {
            currentCall.send(new CallActor.DoEndCall());
        }
    }

    private void stopRunningCall() {
        if (currentCall != null) {
            ActorRef dest = runningCalls.remove(currentCall);
            if (dest != null) {
                dest.send(new CallActor.DoEndCall());
            }
        }
        currentCall = null;
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
        } else if (message instanceof DoCallComplete) {
            DoCallComplete callCreated = (DoCallComplete) message;
            onCallCreated(callCreated.getCallId(), sender());
        } else if (message instanceof IncomingCallReady) {
            IncomingCallReady callComplete = (IncomingCallReady) message;
            onIncomingCallReady(callComplete.getCallId(), sender());
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


    //
    // Call Start
    //

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

    public static class DoCallComplete {

        private long callId;

        public DoCallComplete(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class IncomingCallReady {
        private long callId;

        public IncomingCallReady(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }
}
