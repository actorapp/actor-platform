package im.actor.core.modules.calls;

import java.util.HashMap;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.providers.CallsProvider;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.Command;
import im.actor.runtime.actors.ActorRef;

import static im.actor.runtime.actors.ActorSystem.system;

public class CallsModule extends AbsModule {

    public static final String TAG = "CALLS";

    private CallsProvider provider;
    private ActorRef callManager;
    private HashMap<Long, CallVM> callModels = new HashMap<>();

    public CallsModule(ModuleContext context) {
        super(context);

        provider = context().getConfiguration().getCallsProvider();
    }

    public void run() {
        if (provider == null) {
            return;
        }

        callManager = system().actorOf("calls/manager", CallManagerActor.CONSTRUCTOR(context()));
    }

    public HashMap<Long, CallVM> getCallModels() {
        return callModels;
    }

    public CallVM getCall(long id) {
        return callModels.get(id);
    }

    public ActorRef getCallManager() {
        return callManager;
    }

    public Command<Long> makeCall(final Peer peer) {
        return new Command<Long>() {
            @Override
            public void start(final CommandCallback<Long> callback) {
                callManager.send(new CallManagerActor.DoCall(peer, callback));
            }
        };
    }

    public void muteCall(long callId) {
        callManager.send(new CallManagerActor.MuteCall(callId));
    }

    public void unmuteCall(long callId) {
        callManager.send(new CallManagerActor.UnmuteCall(callId));
    }

    public void endCall(long callId) {
        callManager.send(new CallManagerActor.DoEndCall(callId));
    }

    public void answerCall(long callId) {
        callManager.send(new CallManagerActor.DoAnswerCall(callId));
    }
}
