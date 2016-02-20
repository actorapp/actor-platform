package im.actor.core.modules.calls.peers;

import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class PeerNodeInt extends ActorInterface {

    public PeerNodeInt(ActorRef dest) {
        super(dest);
    }

    public void onOffer(String sdp) {
        send(new PeerNodeActor.OnOfferReceived(sdp));
    }

    public void onAnswer(String sdp) {
        send(new PeerNodeActor.OnAnswerReceived(sdp));
    }

    public void onCandidate(int index, String id, String sdp) {
        send(new PeerNodeActor.OnCandidateReceived(index, id, sdp));
    }

    public void onAdvertised(PeerNodeSettings settings) {
        send(new PeerNodeActor.OnAdvertised(settings));
    }

    public void onOfferNeeded() {
        send(new PeerNodeActor.OnOfferNeeded());
    }

    public void onAnswered() {
        send(new PeerNodeActor.OnAnswered());
    }

    public void setOwnStream(WebRTCMediaStream stream) {
        send(new PeerNodeActor.SetOwnStream(stream));
    }

    public void stop() {
        send(PoisonPill.INSTANCE);
    }
}
