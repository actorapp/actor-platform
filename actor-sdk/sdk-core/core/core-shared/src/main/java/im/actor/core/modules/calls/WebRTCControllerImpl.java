package im.actor.core.modules.calls;

import im.actor.core.entity.signals.AnswerSignal;
import im.actor.core.entity.signals.CandidateSignal;
import im.actor.core.entity.signals.OfferSignal;
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
    public void sendCandidate(int label, String id, String sdp) {
        if (callId != -1) {
            ref.send(new CallManagerActor.SendSignaling(callId, new CandidateSignal(id, label, sdp)));
        }
    }

    @Override
    public void sendOffer(String sdp) {
        if (callId != -1) {
            ref.send(new CallManagerActor.SendSignaling(callId, new OfferSignal(sdp)));
        }
    }

    @Override
    public void sendAnswer(String sdp) {
        if (callId != -1) {
            ref.send(new CallManagerActor.SendSignaling(callId, new AnswerSignal(sdp)));
        }
    }

    @Override
    public void readyForCandidates() {
        if (callId != -1) {
            ref.send(new CallManagerActor.ReadyForCandidates(callId));
        }
    }

    @Override
    public void endCall() {
        if (callId != -1) {
            ref.send(new CallManagerActor.EndCall(callId));
        }
    }
}
