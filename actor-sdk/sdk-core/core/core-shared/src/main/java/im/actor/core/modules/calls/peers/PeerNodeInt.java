package im.actor.core.modules.calls.peers;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.messages.RTCAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCAnswer;
import im.actor.core.modules.calls.peers.messages.RTCCandidate;
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCOwnStart;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;

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
        setDest(system().actorOf(dest.getPath() + "/" + deviceId, new ActorCreator() {
            @Override
            public Actor create() {
                return new PeerNodeActor(deviceId, selfSettings, new WrappedCallback(), context);
            }
        }));
    }


    /**
     * Call this method to set own stream
     *
     * @param stream own stream
     */
    public void setOwnStream(WebRTCMediaStream stream) {
        send(new PeerNodeActor.SetOwnStream(stream));
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
     * Call this method when own user explicitly enables (answers) a call
     */
    public void startOwn() {
        send(new RTCOwnStart());
    }

    /**
     * Call this method when their user explicitly enables (answers) a call
     */
    public void startTheir() {
        send(new RTCStart(deviceId));
    }


    /**
     * Call this method when new offer is needed
     */
    public void onOfferNeeded() {
        send(new RTCNeedOffer(deviceId));
    }

    /**
     * Call this method when offer is received
     *
     * @param sdp sdp of the offer
     */
    public void onOffer(String sdp) {
        send(new RTCOffer(deviceId, sdp));
    }

    /**
     * Call this method when answer is received
     *
     * @param sdp sdp of the answer
     */
    public void onAnswer(String sdp) {
        send(new RTCAnswer(deviceId, sdp));
    }

    /**
     * Call this method when new candidate is received
     *
     * @param index candidate index
     * @param id    candidate id
     * @param sdp   candidate sdp
     */
    public void onCandidate(int index, String id, String sdp) {
        send(new RTCCandidate(deviceId, index, id, sdp));
    }


    private class WrappedCallback implements PeerNodeCallback {

        @Override
        public void onOffer(final long deviceId, final String sdp) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onOffer(deviceId, sdp);
                }
            });
        }

        @Override
        public void onAnswer(final long deviceId, final String sdp) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onAnswer(deviceId, sdp);
                }
            });
        }

        @Override
        public void onCandidate(final long deviceId, final int mdpIndex, final String id, final String sdp) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onCandidate(deviceId, mdpIndex, id, sdp);
                }
            });
        }

        @Override
        public void onHandshakeSuccessful(final long deviceId) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onHandshakeSuccessful(deviceId);
                }
            });
        }

        @Override
        public void onConnectionStarted(final long deviceId) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onConnectionStarted(deviceId);
                }
            });
        }

        @Override
        public void onConnectionEstablished(final long deviceId) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onConnectionEstablished(deviceId);
                }
            });
        }

        @Override
        public void onStreamAdded(final long deviceId, final WebRTCMediaStream stream) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onStreamAdded(deviceId, stream);
                }
            });
        }

        @Override
        public void onStreamRemoved(final long deviceId, final WebRTCMediaStream stream) {
            callbackDest.send(new Runnable() {
                @Override
                public void run() {
                    callback.onStreamRemoved(deviceId, stream);
                }
            });
        }
    }
}
