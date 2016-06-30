package im.actor.core.modules.calls.peers;

import java.util.List;

import im.actor.core.api.ApiICEServer;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.CountedReference;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

import static im.actor.runtime.actors.ActorSystem.system;

/**
 * Peer Connection interface
 */
public class PeerConnectionInt extends ActorInterface {

    private final PeerConnectionCallback callback;
    private final ActorRef callbackDest;

    /**
     * Default constructor for new peer connection
     *
     * @param iceServers    ICE Servers
     * @param ownSettings   setting of own peer
     * @param theirSettings settings of theirs peer
     * @param mediaStream   own media stream
     * @param callback      callback peer connection
     * @param context       core context
     * @param dest          parent actor (used for dispatching callback)
     * @param path          relative path of an actor
     */
    public PeerConnectionInt(List<ApiICEServer> iceServers,
                             PeerSettings ownSettings,
                             PeerSettings theirSettings,
                             CountedReference<WebRTCMediaStream> mediaStream,
                             PeerConnectionCallback callback,
                             ModuleContext context,
                             ActorRef dest, String path) {
        this.callbackDest = dest;
        this.callback = callback;
        ActorRef ref = system().actorOf(dest.getPath() + "/" + path,
                PeerConnectionActor.CONSTRUCTOR(iceServers, ownSettings, theirSettings, mediaStream.acquire(),
                        new WrappedCallback(), context));
        setDest(ref);
    }

    /**
     * Replace Current outgoing stream
     *
     * @param mediaStream media stream
     */
    public Promise<Void> replaceStream(CountedReference<WebRTCMediaStream> mediaStream) {
        return ask(new PeerConnectionActor.ReplaceStream(mediaStream.acquire()));
    }

    /**
     * Call this method to reset current negotiation state
     */
    public void onResetState() {
        send(new PeerConnectionActor.ResetState());
    }

    /**
     * Call this method when peer connection need to generate new offer
     *
     * @param sessionId Session Id
     */
    public void onOfferNeeded(long sessionId) {
        send(new PeerConnectionActor.OnOfferNeeded(sessionId));
    }

    /**
     * Call this method when offer arrived from other peer
     *
     * @param sessionId Session Id
     * @param sdp       sdp of the offer
     */
    public void onOffer(long sessionId, String sdp) {
        send(new PeerConnectionActor.OnOffer(sessionId, sdp));
    }

    /**
     * Call this method when answer arrived from other peer
     *
     * @param sessionId Session Id
     * @param sdp       sdp of the answer
     */
    public void onAnswer(long sessionId, String sdp) {
        send(new PeerConnectionActor.OnAnswer(sessionId, sdp));
    }

    /**
     * Call this method when new candidate arrived from other peer
     *
     * @param index index of media in sdp
     * @param id    id of candidate
     * @param sdp   sdp of candidate
     */
    public void onCandidate(long sessionId, int index, String id, String sdp) {
        send(new PeerConnectionActor.OnCandidate(sessionId, index, id, sdp));
    }


    /**
     * Wrapped PeerConnectionCallback that dispatches events on specific actor
     */
    private class WrappedCallback implements PeerConnectionCallback {

        @Override
        public void onOffer(long sessionId, String sdp) {
            callbackDest.post(() -> callback.onOffer(sessionId, sdp));
        }

        @Override
        public void onAnswer(long sessionId, String sdp) {
            callbackDest.post(() -> callback.onAnswer(sessionId, sdp));
        }

        @Override
        public void onCandidate(long sessionId, int mdpIndex, String id, String sdp) {
            callbackDest.post(() -> callback.onCandidate(sessionId, mdpIndex, id, sdp));
        }

        @Override
        public void onNegotiationSuccessful(long sessionId) {
            callbackDest.post(() -> callback.onNegotiationSuccessful(sessionId));
        }

        @Override
        public void onNegotiationNeeded(long sessionId) {
            callbackDest.post(() -> callback.onNegotiationNeeded(sessionId));
        }

        @Override
        public void onStreamAdded(WebRTCMediaStream stream) {
            callbackDest.post(() -> callback.onStreamAdded(stream));
        }

        @Override
        public void onStreamRemoved(WebRTCMediaStream stream) {
            callbackDest.post(() -> callback.onStreamRemoved(stream));
        }
    }
}