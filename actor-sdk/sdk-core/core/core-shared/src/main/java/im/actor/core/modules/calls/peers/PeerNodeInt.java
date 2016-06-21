package im.actor.core.modules.calls.peers;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiICEServer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.messages.RTCAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCAnswer;
import im.actor.core.modules.calls.peers.messages.RTCCandidate;
import im.actor.core.modules.calls.peers.messages.RTCMasterAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCMediaStateUpdated;
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCCloseSession;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.CountedReference;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;

import static im.actor.runtime.actors.ActorSystem.system;

/**
 * Peer Node. Abstraction on top of PeerConnection that handles pre connection logic and staged
 * initialization.
 * For fully starting node you need to call startOwn(), startTheir(), onAdvertised()
 * and setOwnStream() methods.
 */
public class PeerNodeInt extends ActorInterface {

    private final long deviceId;
    private final ActorRef callbackDest;
    private final PeerNodeCallback callback;

    public PeerNodeInt(final long deviceId, PeerNodeCallback callback, final PeerSettings selfSettings, ActorRef dest,
                       final ModuleContext context) {
        this.callbackDest = dest;
        this.deviceId = deviceId;
        this.callback = callback;
        setDest(system().actorOf(dest.getPath() + "/" + deviceId, () -> {
            return new PeerNodeActor(deviceId, selfSettings, new WrappedCallback(), context);
        }));
    }


    /**
     * Call this method to set own stream
     *
     * @param stream own stream
     */
    public Promise<Void> replaceOwnStream(CountedReference<WebRTCMediaStream> stream) {
        return ask(new PeerNodeActor.ReplaceOwnStream(stream.acquire()));
    }

    /**
     * Call this method when master was advertised
     *
     * @param iceServers ice servers
     */
    public void onAdvertisedMaster(List<ApiICEServer> iceServers) {
        send(new RTCMasterAdvertised(new ArrayList<>(iceServers)));
    }

    /**
     * Call this method when peer was advertised
     *
     * @param settings settings of the peer
     */
    public void onAdvertised(PeerSettings settings) {
        send(new RTCAdvertised(deviceId, settings));
    }

    /**
     * Call this method when both users explicitly enables (answers) a call
     */
    public void startConnection() {
        send(new RTCStart(deviceId));
    }

    /**
     * Call this method when new offer is needed
     *
     * @param sessionId Session Id
     */
    public void onOfferNeeded(long sessionId) {
        send(new RTCNeedOffer(deviceId, sessionId));
    }

    /**
     * Call this method when offer is received
     *
     * @param sessionId Session Id of offer
     * @param sdp       sdp of the offer
     */
    public void onOffer(long sessionId, String sdp) {
        send(new RTCOffer(deviceId, sessionId, sdp));
    }

    /**
     * Call this method when answer is received
     *
     * @param sessionId Session Id
     * @param sdp       sdp of the answer
     */
    public void onAnswer(long sessionId, String sdp) {
        send(new RTCAnswer(deviceId, sessionId, sdp));
    }

    /**
     * Call this method when new candidate is received
     *
     * @param index candidate index
     * @param id    candidate id
     * @param sdp   candidate sdp
     */
    public void onCandidate(long sessionId, int index, String id, String sdp) {
        send(new RTCCandidate(deviceId, sessionId, index, id, sdp));
    }

    /**
     * Call this method when new media state is received
     *
     * @param isAudioEnabled is audio streams enabled
     * @param isVideoEnabled is video streams enabled
     */
    public void onMediaStateChanged(boolean isAudioEnabled, boolean isVideoEnabled) {
        send(new RTCMediaStateUpdated(deviceId, isAudioEnabled, isVideoEnabled));
    }

    /**
     * Resetting handshake state of the node
     *
     * @param sessionId Sesison Id
     */
    public void closeSession(long sessionId) {
        send(new RTCCloseSession(deviceId, sessionId));
    }


    private class WrappedCallback implements PeerNodeCallback {

        @Override
        public void onOffer(long deviceId, long sessionId, String sdp) {
            callbackDest.post(() -> callback.onOffer(deviceId, sessionId, sdp));
        }

        @Override
        public void onAnswer(long deviceId, long sessionId, String sdp) {
            callbackDest.post(() -> callback.onAnswer(deviceId, sessionId, sdp));
        }

        @Override
        public void onNegotiationSuccessful(long deviceId, long sessionId) {
            callbackDest.post(() -> callback.onNegotiationSuccessful(deviceId, sessionId));
        }

        @Override
        public void onNegotiationNeeded(long deviceId, long sessionId) {
            callbackDest.post(() -> callback.onNegotiationNeeded(deviceId, sessionId));
        }

        @Override
        public void onCandidate(long deviceId, long sessionId, int mdpIndex, String id, String sdp) {
            callbackDest.post(() -> callback.onCandidate(deviceId, sessionId, mdpIndex, id, sdp));
        }

        @Override
        public void onPeerStateChanged(long deviceId, PeerState state) {
            callbackDest.post(() -> callback.onPeerStateChanged(deviceId, state));
        }

        @Override
        public void onTrackAdded(long deviceId, WebRTCMediaTrack track) {
            callbackDest.post(() -> callback.onTrackAdded(deviceId, track));
        }

        @Override
        public void onTrackRemoved(long deviceId, WebRTCMediaTrack track) {
            callbackDest.post(() -> callback.onTrackRemoved(deviceId, track));
        }
    }
}
