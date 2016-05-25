package im.actor.core.modules.calls.peers;

import java.util.List;

import im.actor.core.api.ApiICEServer;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
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
                             WebRTCMediaStream mediaStream,
                             PeerConnectionCallback callback,
                             ModuleContext context,
                             ActorRef dest, String path) {
        this.callbackDest = dest;
        this.callback = callback;
        ActorRef ref = system().actorOf(dest.getPath() + "/" + path,
                PeerConnectionActor.CONSTRUCTOR(iceServers, ownSettings, theirSettings, mediaStream,
                        new WrappedCallback(), context));
        setDest(ref);
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
    public void onCandidate(int index, String id, String sdp) {
        send(new PeerConnectionActor.OnCandidate(index, id, sdp));
    }


    /**
     * Wrapped PeerConnectionCallback that dispatches events on specific actor
     */
    private class WrappedCallback implements PeerConnectionCallback {

        @Override
        public void onOffer(final long sessionId, final String sdp) {
            callbackDest.send((Runnable) () -> callback.onOffer(sessionId, sdp));
        }

        @Override
        public void onAnswer(final long sessionId, final String sdp) {
            callbackDest.send((Runnable) () -> callback.onAnswer(sessionId, sdp));
        }

        @Override
        public void onCandidate(final int mdpIndex, final String id, final String sdp) {
            callbackDest.send((Runnable) () -> callback.onCandidate(mdpIndex, id, sdp));
        }

        @Override
        public void onNegotiationSuccessful(final long sessionId) {
            callbackDest.send((Runnable) () -> callback.onNegotiationSuccessful(sessionId));
        }

        @Override
        public void onStreamAdded(final WebRTCMediaStream stream) {
            callbackDest.send((Runnable) () -> callback.onStreamAdded(stream));
        }

        @Override
        public void onStreamRemoved(final WebRTCMediaStream stream) {
            callbackDest.send((Runnable) () -> callback.onStreamRemoved(stream));
        }

        @Override
        public void onPeerConnectionCreated(WebRTCPeerConnection peerConnection) {
            callbackDest.send((Runnable) () -> callback.onPeerConnectionCreated(peerConnection));
        }
    }
}