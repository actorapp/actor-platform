package im.actor.core.modules.calls;

import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.AbsCallActor;
import im.actor.core.modules.ModuleActor;
import im.actor.core.providers.CallsProvider;
import im.actor.core.util.RandomUtils;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Constructor;
import im.actor.runtime.power.WakeLock;

public class CallManagerActor extends ModuleActor {

    public static ActorCreator CONSTRUCTOR(final ModuleContext context) {
        return () -> new CallManagerActor(context);
    }

    private static final String TAG = "CallManagerActor";

    private CallsProvider provider;
    private HashSet<Long> handledCalls = new HashSet<>();
    private HashMap<Long, Integer> handledCallAttempts = new HashMap<>();
    private HashSet<Long> answeredCalls = new HashSet<>();

    private Long currentCall;
    private HashMap<Long, ActorRef> runningCalls = new HashMap<>();
    private boolean isBeeping = false;

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
        if (currentCall != null) {
            terminalCall(currentCall);
            currentCall = null;
        }

        //
        // Spawning new Actor for call
        //
        final WakeLock wakeLock = Runtime.makeWakeLock();
        system().actorOf("actor/master/" + RandomUtils.nextRid(), () -> {
            return new CallActor(peer, callback, wakeLock, context());
        });
    }

    private void onCallCreated(long callId, ActorRef ref) {

        //
        // Stopping current call some are started during call establishing
        //
        if (currentCall != null) {
            terminalCall(currentCall);
            currentCall = null;

            if (isBeeping) {
                isBeeping = false;
                provider.stopOutgoingBeep();
            }
        }

        //
        // Saving Reference to call
        //
        runningCalls.put(callId, ref);

        //
        // Marking outgoing call as answered
        //
        answeredCalls.add(callId);

        //
        // Setting Current Call
        //
        currentCall = callId;

        //
        // Notify Provider about new current call
        //
        provider.onCallStart(callId);
        isBeeping = true;
        provider.startOutgoingBeep();
    }


    //
    // Incoming call
    //

    private void onIncomingCall(final long callId, final int attempt, WakeLock wakeLock) {
        Log.d(TAG, "onIncomingCall (" + callId + ")");

        //
        // Filter double updates about incoming call
        //
        if (handledCalls.contains(callId)) {
            if (handledCallAttempts.get(callId) >= attempt) {
                if (wakeLock != null) {
                    wakeLock.releaseLock();
                }
                return;
            }
        }

        //
        // Ignore any incoming call if we already have running call with such call id
        //
        if (runningCalls.containsKey(callId)) {
            if (wakeLock != null) {
                wakeLock.releaseLock();
            }
            return;
        }

        //
        // Marking handled calls as handled
        //
        handledCalls.add(callId);
        handledCallAttempts.put(callId, attempt);

        //
        // Creating wake lock if needed
        //
        if (wakeLock == null) {
            wakeLock = Runtime.makeWakeLock();
        }

        //
        // Spawning new Actor for call
        //
        final WakeLock finalWakeLock = wakeLock;
        system().actorOf("actor/call" + RandomUtils.nextRid(), () -> {
            return new CallActor(callId, finalWakeLock, context());
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

        // If We are not answered this call on this device
        if (!answeredCalls.contains(callId)) {

            //
            // Notify provider
            //
            if (currentCall != null && currentCall == callId) {
                currentCall = null;
                provider.onCallEnd(callId);
            }

            //
            // Shutdown call actor
            //
            terminalCall(callId);
        }
    }

    private void doAnswerCall(final long callId) {
        Log.d(TAG, "doAnswerCall (" + callId + ")");

        // If not already answered
        if (!answeredCalls.contains(callId)) {

            //
            // Mark as answered
            //
            answeredCalls.add(callId);

            //
            // Sending answer message to actor.
            //
            ActorRef ref = runningCalls.get(callId);
            if (ref != null) {
                ref.send(new CallActor.AnswerCall());
            }

            //
            // Notify Provider to stop playing ringtone
            //
            if (currentCall != null && currentCall == callId) {
                provider.onCallAnswered(callId);
            }
        }
    }

    private void onCallAnswered(long callId) {
        Log.d(TAG, "onCallAnswered (" + callId + ")");
        if (currentCall == callId) {
            if (isBeeping) {
                isBeeping = false;
                provider.stopOutgoingBeep();
            }

            provider.onCallAnswered(callId);
        }
    }

    //
    // Call Mute/Unmute
    //
    private void onCallMute(long callId) {
        ActorRef ref = runningCalls.get(callId);
        if (ref != null) {
            ref.send(new AbsCallActor.MuteChanged(true));
        }
    }

    private void onCallUnmute(long callId) {
        ActorRef ref = runningCalls.get(callId);
        if (ref != null) {
            ref.send(new AbsCallActor.MuteChanged(false));
        }
    }

    //
    // Call video disable/enable
    //
    private void onCallVideoEnable(long callId) {
        ActorRef ref = runningCalls.get(callId);
        if (ref != null) {
            ref.send(new AbsCallActor.VideoEnabled(true));
        }
    }

    private void onCallVideoDisable(long callId) {
        ActorRef ref = runningCalls.get(callId);
        if (ref != null) {
            ref.send(new AbsCallActor.VideoEnabled(false));
        }
    }

    //
    // Ending call
    //

    private void onCallEnded(long callId) {
        Log.d(TAG, "onCallEnded (" + callId + ")");

        //
        // Event ALWAYS comes from Call Actor and we doesn't need
        // to stop it explicitly.
        //
        // Removing from running calls
        //
        runningCalls.remove(callId);

        //
        // Notify Provider if this call was current
        //
        if (currentCall != null && currentCall == callId) {
            currentCall = null;
            provider.onCallEnd(callId);

            if (isBeeping) {
                isBeeping = false;
                provider.stopOutgoingBeep();
            }
        }
    }

    private void doEndCall(long callId) {
        Log.d(TAG, "doEndCall (" + callId + ")");

        //
        // Action ALWAYS comes from UI side and we need only stop call actor
        // explicitly and it will do the rest.
        //
        ActorRef currentCallActor = runningCalls.remove(callId);
        if (currentCallActor != null) {
            if (answeredCalls.contains(callId)) {
                currentCallActor.send(PoisonPill.INSTANCE);
            } else {
                currentCallActor.send(new CallActor.RejectCall());
            }
        }

        //
        // Notify Provider if this call was current
        //
        if (currentCall != null && currentCall == callId) {
            currentCall = null;
            provider.onCallEnd(callId);
            if (isBeeping) {
                isBeeping = false;
                provider.stopOutgoingBeep();
            }
        }
    }

    private void probablyEndCall() {
        if (currentCall != null) {
            doEndCall(currentCall);
        }
    }

    private void terminalCall(long callId) {
        ActorRef dest = runningCalls.remove(callId);
        if (dest != null) {
            dest.send(PoisonPill.INSTANCE);
        }
    }

    private void sendToCall(long callId, Object message) {
        ActorRef dest = runningCalls.get(callId);
        if (dest != null) {
            dest.send(message);
        }
    }


    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnIncomingCall) {
            OnIncomingCall call = (OnIncomingCall) message;
            onIncomingCall(call.getCallId(), call.getAttempt(), null);
        } else if (message instanceof OnIncomingCallLocked) {
            OnIncomingCallLocked locked = (OnIncomingCallLocked) message;
            onIncomingCall(locked.getCallId(), locked.getAttempt(), locked.getWakeLock());
        } else if (message instanceof OnIncomingCallHandled) {
            OnIncomingCallHandled incomingCallHandled = (OnIncomingCallHandled) message;
            onIncomingCallHandled(incomingCallHandled.getCallId());
        } else if (message instanceof DoAnswerCall) {
            doAnswerCall(((DoAnswerCall) message).getCallId());
        } else if (message instanceof DoEndCall) {
            doEndCall(((DoEndCall) message).getCallId());
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
        } else if (message instanceof OnCallAnswered) {
            OnCallAnswered answered = (OnCallAnswered) message;
            onCallAnswered(answered.getCallId());
        } else if (message instanceof MuteCall) {
            onCallMute(((MuteCall) message).getCallId());
        } else if (message instanceof UnmuteCall) {
            onCallUnmute(((UnmuteCall) message).getCallId());
        } else if (message instanceof DisableVideo) {
            onCallVideoDisable(((DisableVideo) message).getCallId());
        } else if (message instanceof EnableVideo) {
            onCallVideoEnable(((EnableVideo) message).getCallId());
        } else if (message instanceof ProbablyEndCall) {
            probablyEndCall();
        } else {
            super.onReceive(message);
        }
    }

    public static class OnIncomingCall {

        private long callId;
        private int attempt;

        public OnIncomingCall(long callId, int attempt) {
            this.callId = callId;
            this.attempt = attempt;
        }

        public long getCallId() {
            return callId;
        }

        public int getAttempt() {
            return attempt;
        }
    }

    public static class OnIncomingCallLocked {

        private long callId;
        private int attempt;
        private WakeLock wakeLock;

        public OnIncomingCallLocked(long callId, int attempt, WakeLock wakeLock) {
            this.callId = callId;
            this.wakeLock = wakeLock;
            this.attempt = attempt;
        }

        public long getCallId() {
            return callId;
        }

        public WakeLock getWakeLock() {
            return wakeLock;
        }

        public int getAttempt() {
            return attempt;
        }
    }

    public static class OnIncomingCallHandled {

        private long callId;
        private int attempt;

        public OnIncomingCallHandled(long callId, int attempt) {
            this.callId = callId;
            this.attempt = attempt;
        }

        public int getAttempt() {
            return attempt;
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

    public static class DoAnswerCall {

        private long callId;

        public DoAnswerCall(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class DoEndCall {
        private long callId;

        public DoEndCall(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }


    //
    // Call State
    //

    public static class MuteCall {
        private long callId;

        public MuteCall(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class UnmuteCall {
        private long callId;

        public UnmuteCall(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class DisableVideo {
        private long callId;

        public DisableVideo(long callId) {
            this.callId = callId;
        }

        public long getCallId() {
            return callId;
        }
    }

    public static class EnableVideo {
        private long callId;

        public EnableVideo(long callId) {
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

    public static class OnCallAnswered {
        private long callId;

        public OnCallAnswered(long callId) {
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

    public static class ProbablyEndCall {

    }
}
