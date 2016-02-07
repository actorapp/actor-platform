package im.actor.core.modules.calls;

import im.actor.core.entity.signals.AbsSignal;
import im.actor.core.webrtc.WebRTCController;
import im.actor.runtime.actors.ActorRef;

public class WebRTCControllerImpl implements WebRTCController {

    private long callId = -1;
    private ActorRef ref;

    public WebRTCControllerImpl(ActorRef ref) {
        this.ref = ref;
    }

    public long getCallId() {
        return callId;
    }

    public void switchCallId(long callId) {
        this.callId = callId;
    }

    public void clearCallId() {
        this.callId = -1;
    }

    @Override
    public void answerCall() {
        if (callId != -1) {
            ref.send(new CallManagerActor.AnswerCall(callId));
        }
    }

    @Override
    public void sendSignaling(AbsSignal signal) {
        if (callId != -1) {
            ref.send(new CallManagerActor.SendSignaling(callId, signal));
        }
    }

    @Override
    public void endCall() {

    }
}
