package im.actor.core.modules.calls;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.providers.CallsProvider;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.Command;
import im.actor.runtime.*;
import im.actor.runtime.actors.ActorRef;

import static im.actor.runtime.actors.ActorSystem.system;

public class CallsModule extends AbsModule {

    public static final String TAG = "CALLS";

    private ActorRef callManager;
    private CallViewModels callViewModels;

    public CallsModule(ModuleContext context) {
        super(context);

        if (context().getConfiguration().isVoiceCallsEnabled()) {
            callViewModels = new CallViewModels(context());
        }
    }

    public void run() {
        if (context().getConfiguration().isVoiceCallsEnabled()) {
            callManager = system().actorOf("calls/manager", CallManagerActor.CONSTRUCTOR(context()));
        }
    }

    public CallViewModels getCallViewModels() {
        return callViewModels;
    }

    public CallVM getCall(long id) {
        return callViewModels.getCall(id);
    }

    public ActorRef getCallManager() {
        return callManager;
    }

    public void checkCall(long callId, int attempt) {
        callManager.send(new CallManagerActor.OnIncomingCallLocked(callId, attempt, im.actor.runtime.Runtime.makeWakeLock()));
    }

    public void probablyEndCall() {
        callManager.send(new CallManagerActor.ProbablyEndCall());
    }

    public Command<Long> makeCall(final Peer peer) {
        return callback -> callManager.send(new CallManagerActor.DoCall(peer, callback));
    }

    public void muteCall(long callId) {
        callManager.send(new CallManagerActor.MuteCall(callId));
    }

    public void unmuteCall(long callId) {
        callManager.send(new CallManagerActor.UnmuteCall(callId));
    }

    public void disableVideo(long callId) {
        callManager.send(new CallManagerActor.DisableVideo(callId));
    }

    public void enableVideo(long callId) {
        callManager.send(new CallManagerActor.EnableVideo(callId));
    }

    public void endCall(long callId) {
        callManager.send(new CallManagerActor.DoEndCall(callId));
    }

    public void answerCall(long callId) {
        callManager.send(new CallManagerActor.DoAnswerCall(callId));
    }
}
