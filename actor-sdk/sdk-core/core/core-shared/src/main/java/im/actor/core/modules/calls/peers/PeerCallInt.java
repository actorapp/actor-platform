package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiICEServer;
import im.actor.core.modules.calls.peers.messages.RTCAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCAnswer;
import im.actor.core.modules.calls.peers.messages.RTCCandidate;
import im.actor.core.modules.calls.peers.messages.RTCCloseSession;
import im.actor.core.modules.calls.peers.messages.RTCDispose;
import im.actor.core.modules.calls.peers.messages.RTCMasterAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCMediaStateUpdated;
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;

public class PeerCallInt extends ActorInterface {

    public PeerCallInt(ActorRef dest) {
        super(dest);
    }


    public void onConfigurationReady(List<ApiICEServer> iceServers) {
        send(new RTCMasterAdvertised(new ArrayList<>(iceServers)));
    }

    public void onTheirStarted(long deviceId) {
        send(new RTCStart(deviceId));
    }

    public void onOwnStarted() {
        send(new PeerCallActor.OwnStarted());
    }

    public void onAdvertised(long deviceId, @NotNull PeerSettings settings) {
        send(new RTCAdvertised(deviceId, settings));
    }

    public void onOffer(long deviceId, long sessionId, String sdp) {
        send(new RTCOffer(deviceId, sessionId, sdp));
    }

    public void onAnswer(long deviceId, long sessionId, String sdp) {
        send(new RTCAnswer(deviceId, sessionId, sdp));
    }

    public void onCandidate(long deviceId, long sessionId, int mdpIndex, String id, String sdp) {
        send(new RTCCandidate(deviceId, sessionId, mdpIndex, id, sdp));
    }

    public void onOfferNeeded(long deviceId, long sessionId) {
        send(new RTCNeedOffer(deviceId, sessionId));
    }

    public void onMediaStateChanged(long deviceId, boolean isAudioEnabled, boolean isVideoEnabled) {
        send(new RTCMediaStateUpdated(deviceId, isAudioEnabled, isVideoEnabled));
    }

    public void closeSession(long deviceId, long sessionId) {
        send(new RTCCloseSession(deviceId, sessionId));
    }

    public void disposePeer(long deviceId) {
        send(new RTCDispose(deviceId));
    }

    public void onAudioEnabledChanged(boolean enabled) {
        send(new PeerCallActor.AudioEnabled(enabled));
    }

    public void onVideoEnabledChanged(boolean enabled) {
        send(new PeerCallActor.VideoEnabled(enabled));
    }
}