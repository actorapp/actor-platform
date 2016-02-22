package im.actor.core.modules.calls.peers;

import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;

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
     * @param ownSettings   setting of own peer
     * @param theirSettings settings of theirs peer
     * @param mediaStream   own media stream
     * @param callback      callback peer connection
     * @param context       core context
     * @param dest          parent actor (used for dispatching callback)
     * @param path          relative path of an actor
     */
    public PeerConnectionInt(PeerSettings ownSettings,
                             PeerSettings theirSettings,
                             WebRTCMediaStream mediaStream,
                             PeerConnectionCallback callback,
                             ModuleContext context,
                             ActorRef dest, String path) {
        this.callbackDest = dest;
        this.callback = callback;
        ActorRef ref = system().actorOf(dest.getPath() + "/" + path,
                PeerConnectionActor.CONSTRUCTOR(ownSettings, theirSettings, mediaStream,
                        new WrappedCallback(), context));
        setDest(ref);
    }

    /**
     * Call this method when peer connection need to generate new offer
     */
    public void onOfferNeeded() {
        send(new PeerConnectionActor.OnOfferNeeded());
    }

    /**
     * Call this method when offer arrived from other peer
     *
     * @param sdp sdp of the offer
     */
    public void onOffer(String sdp) {
        send(new PeerConnectionActor.OnOffer(sdp));
    }

    /**
     * Call this method when answer arrived from other peer
     *
     * @param sdp sdp of the answer
     */
    public void onAnswer(String sdp) {
        send(new PeerConnectionActor.OnAnswer(sdp));
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
        public void onOffer(final String sdp) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onOffer(sdp);
                }
            });
        }

        @Override
        public void onAnswer(final String sdp) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onAnswer(sdp);
                }
            });
        }

        @Override
        public void onCandidate(final int mdpIndex, final String id, final String sdp) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onCandidate(mdpIndex, id, sdp);
                }
            });
        }

        @Override
        public void onStreamAdded(final WebRTCMediaStream stream) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onStreamAdded(stream);
                }
            });
        }

        @Override
        public void onStreamRemoved(final WebRTCMediaStream stream) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onStreamRemoved(stream);
                }
            });
        }
    }
}