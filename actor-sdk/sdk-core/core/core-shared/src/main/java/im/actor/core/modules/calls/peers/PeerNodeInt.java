package im.actor.core.modules.calls.peers;

import im.actor.core.modules.calls.entity.PeerNodeSettings;
import im.actor.core.modules.calls.peers.messages.RTCAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCAnswer;
import im.actor.core.modules.calls.peers.messages.RTCCandidate;
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCOwnStart;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class PeerNodeInt extends ActorInterface {

    private final long deviceId;

    public PeerNodeInt(long deviceId, ActorRef dest) {
        super(dest);
        this.deviceId = deviceId;
    }

    public void onOffer(String sdp) {
        send(new RTCOffer(deviceId, sdp));
    }

    public void onAnswer(String sdp) {
        send(new RTCAnswer(deviceId, sdp));
    }

    public void onCandidate(int index, String id, String sdp) {
        send(new RTCCandidate(deviceId, index, id, sdp));
    }

    public void onAdvertised(PeerNodeSettings settings) {
        send(new RTCAdvertised(deviceId, settings));
    }

    public void onOfferNeeded() {
        send(new RTCNeedOffer(deviceId));
    }

    public void setOwnStream(WebRTCMediaStream stream) {
        send(new PeerNodeActor.SetOwnStream(stream));
    }

    public void startOwn() {
        send(new RTCOwnStart());
    }

    public void startTheir() {
        send(new RTCStart(deviceId));
    }
}
