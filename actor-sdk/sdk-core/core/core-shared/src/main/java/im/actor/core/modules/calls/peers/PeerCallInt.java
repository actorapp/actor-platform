package im.actor.core.modules.calls.peers;

import im.actor.core.modules.calls.peers.messages.RTCAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCAnswer;
import im.actor.core.modules.calls.peers.messages.RTCCandidate;
import im.actor.core.modules.calls.peers.messages.RTCDispose;
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCOwnStart;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;

public class PeerCallInt extends ActorInterface {

    public PeerCallInt(ActorRef dest) {
        super(dest);
    }


    public void onTheirStarted(long deviceId) {
        send(new RTCStart(deviceId));
    }

    public void onOwnStarted() {
        send(new RTCOwnStart());
    }

    public void onAdvertised(long deviceId, PeerSettings settings) {
        send(new RTCAdvertised(deviceId, settings));
    }


    public void onOffer(long deviceId, String sdp) {
        send(new RTCOffer(deviceId, sdp));
    }

    public void onAnswer(long deviceId, String sdp) {
        send(new RTCAnswer(deviceId, sdp));
    }

    public void onCandidate(long deviceId, int mdpIndex, String id, String sdp) {
        send(new RTCCandidate(deviceId, mdpIndex, id, sdp));
    }

    public void onOfferNeeded(long deviceId) {
        send(new RTCNeedOffer(deviceId));
    }

    public void disposePeer(long deviceId) {
        send(new RTCDispose(deviceId));
    }
}
