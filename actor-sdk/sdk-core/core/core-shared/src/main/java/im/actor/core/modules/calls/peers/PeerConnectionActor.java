package im.actor.core.modules.calls.peers;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import im.actor.core.api.ApiICEServer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.collections.ManagedList;
import im.actor.runtime.function.CountedReference;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCPeerConnectionCallback;
import im.actor.runtime.webrtc.WebRTCSessionDescription;
import im.actor.runtime.webrtc.WebRTCSettings;

/*-[
#pragma clang diagnostic ignored "-Wnullability-completeness"
]-*/

public class PeerConnectionActor extends ModuleActor {

    @NotNull
    public static ActorCreator CONSTRUCTOR(@NotNull final List<ApiICEServer> iceServers,
                                           @NotNull final PeerSettings selfSettings,
                                           @NotNull final PeerSettings theirSettings,
                                           @NotNull final CountedReference<WebRTCMediaStream> userMedia,
                                           @NotNull final PeerConnectionCallback callback,
                                           @NotNull final ModuleContext context) {
        return () -> new PeerConnectionActor(iceServers, selfSettings, theirSettings, userMedia, callback, context);
    }


    private final String TAG;

    @NotNull
    private final List<ApiICEServer> iceServers;
    @NotNull
    private final PeerConnectionCallback callback;
    @Nullable
    private CountedReference<WebRTCMediaStream> userMedia;

    @NotNull
    private WebRTCPeerConnection peerConnection;
    private boolean isReady = false;
    private boolean isReadyForCandidates = false;
    @NotNull
    private PeerConnectionState state = PeerConnectionState.INITIALIZATION;

    private long sessionId = -1;

    public PeerConnectionActor(@NotNull List<ApiICEServer> iceServers,
                               @NotNull PeerSettings selfSettings,
                               @NotNull PeerSettings theirSettings,
                               @NotNull CountedReference<WebRTCMediaStream> userMedia,
                               @NotNull PeerConnectionCallback callback,
                               @NotNull ModuleContext context) {
        super(context);
        this.TAG = "PeerConnection";
        this.callback = callback;
        this.userMedia = userMedia;
        this.iceServers = iceServers;
    }

    @Override
    public void preStart() {

        isReady = false;
        WebRTCIceServer[] rtcIceServers = ManagedList.of(iceServers).map(apiICEServer -> new WebRTCIceServer(apiICEServer.getUrl(), apiICEServer.getUsername(), apiICEServer.getCredential())).toArray(new WebRTCIceServer[0]);
        WebRTCSettings settings = new WebRTCSettings(false, false, config().isVideoCallsEnabled());
        WebRTC.createPeerConnection(rtcIceServers, settings).then(webRTCPeerConnection -> {
            PeerConnectionActor.this.peerConnection = webRTCPeerConnection;
            PeerConnectionActor.this.peerConnection.addOwnStream(userMedia);
            PeerConnectionActor.this.peerConnection.addCallback(new WebRTCPeerConnectionCallback() {

                @Override
                public void onCandidate(int label, String id, String candidate) {
                    if (sessionId != -1) {
                        callback.onCandidate(sessionId, label, id, candidate);
                    }
                }

                @Override
                public void onRenegotiationNeeded() {
//                    if (sessionId != -1) {
//                        callback.onNegotiationNeeded(sessionId);
//                    }
                }

                @Override
                public void onStreamAdded(WebRTCMediaStream addedStream) {
                    // Disabling all tracks and make it needed to be explicitly enabled
                    // by parent actor
                    for (WebRTCMediaTrack track : addedStream.getAudioTracks()) {
                        track.setEnabled(false);
                    }
                    for (WebRTCMediaTrack track : addedStream.getVideoTracks()) {
                        track.setEnabled(false);
                    }
                    callback.onStreamAdded(addedStream);
                }

                @Override
                public void onStreamRemoved(WebRTCMediaStream removedStream) {
                    callback.onStreamRemoved(removedStream);
                }

                @Override
                public void onDisposed() {

                }
            });
            state = PeerConnectionState.WAITING_HANDSHAKE;
            onReady();
        }).failure(e -> {
            Log.d(TAG, "preStart:error");
            e.printStackTrace();
            onHandshakeFailure();
        });
    }

    public void onResetState() {

        //
        // Just Reset current state
        //
        state = PeerConnectionState.WAITING_HANDSHAKE;
        sessionId = -1;
    }

    public void onReplaceStream(CountedReference<WebRTCMediaStream> newStream) {
        if (userMedia != null) {
            peerConnection.removeOwnStream(userMedia);
            userMedia.release();
        }
        peerConnection.addOwnStream(newStream);
        userMedia = newStream;
        if (sessionId != -1) {
            callback.onNegotiationNeeded(sessionId);
        }
    }

    public void onOfferNeeded(final long sessionId) {

        // Ignore if we are not waiting for handshake
        if (state != PeerConnectionState.WAITING_HANDSHAKE) {
            return;
        }

        // Ignore if we already have session id
        if (this.sessionId != -1) {
            return;
        }

        //
        // Stages
        // 1. Create Offer
        // 2. Set Local Description
        // 3. Send Offer
        //

        isReady = false;
        peerConnection.createOffer().flatMap(webRTCSessionDescription -> {
            return peerConnection.setLocalDescription(SDPOptimizer.optimize(webRTCSessionDescription));
        }).then(webRTCSessionDescription -> {
            // Save Session Id and switch to next state
            PeerConnectionActor.this.sessionId = sessionId;
            PeerConnectionActor.this.state = PeerConnectionState.WAITING_ANSWER;
            callback.onOffer(sessionId, webRTCSessionDescription.getSdp());
            onReady();
        }).failure(e -> {
            e.printStackTrace();
            onHandshakeFailure();
        });
    }

    public void onOffer(final long sessionId, @NotNull String sdp) {

        // Ignore if we are not waiting for handshake
        if (state != PeerConnectionState.WAITING_HANDSHAKE) {
            return;
        }

        // Ignore if we already have session id
        if (this.sessionId != -1) {
            return;
        }

        //
        // Stages
        // 1. Set Remote Description
        // 2. Create Answer
        // 3. Set Local Description
        // 4. Send Answer
        // 5. Enter READY mode
        //

        isReady = false;
        peerConnection.setRemoteDescription(new WebRTCSessionDescription("offer", sdp)).flatMap(description -> {
            return peerConnection.createAnswer();
        }).flatMap(webRTCSessionDescription -> {
            return peerConnection.setLocalDescription(SDPOptimizer.optimize(webRTCSessionDescription));
        }).then(webRTCSessionDescription -> {
            callback.onAnswer(sessionId, webRTCSessionDescription.getSdp());
            PeerConnectionActor.this.sessionId = sessionId;
            onHandShakeCompleted(sessionId);
            onReady();
        }).failure(e -> {
            e.printStackTrace();
            onHandshakeFailure();
        });
    }

    public void onAnswer(final long sessionId, @NotNull String sdp) {
        // Ignore if we are not waiting for answer
        if (state != PeerConnectionState.WAITING_ANSWER) {
            return;
        }

        //
        // Stages
        // 1. Set Remote Description
        // 2. Enter READY mode
        //

        peerConnection.setRemoteDescription(new WebRTCSessionDescription("answer", sdp)).then(description -> {
            onHandShakeCompleted(sessionId);
            onReady();
        }).failure(e -> {
            e.printStackTrace();
            onHandshakeFailure();
        });
    }

    private void onReady() {
        isReady = true;
        unstashAll();
    }

    private void onHandshakeFailure() {
        isReady = false;
        isReadyForCandidates = false;
        state = PeerConnectionState.CLOSED;
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        if (userMedia != null) {
            userMedia.release();
            userMedia = null;
        }
    }

    private void onHandShakeCompleted(long sessionId) {
        callback.onNegotiationSuccessful(sessionId);
        isReadyForCandidates = true;
        state = PeerConnectionState.READY;
        unstashAll();
    }

    public void onCandidate(int index, @NotNull String id, @NotNull String sdp) {
        peerConnection.addCandidate(index, id, sdp);
    }

    @Override
    public void postStop() {
        super.postStop();

        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        if (userMedia != null) {
            userMedia.release();
            userMedia = null;
        }

        isReady = false;
        state = PeerConnectionState.CLOSED;
    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnOffer) {
            if (!isReady) {
                stash();
                return;
            }
            OnOffer offer = (OnOffer) message;
            onOffer(offer.getSessionId(), offer.getSdp());
        } else if (message instanceof OnAnswer) {
            if (!isReady) {
                stash();
                return;
            }
            OnAnswer answer = (OnAnswer) message;
            onAnswer(answer.getSessionId(), answer.getSdp());
        } else if (message instanceof OnCandidate) {
            if (!isReady || !isReadyForCandidates) {
                stash();
                return;
            }
            OnCandidate candidate = (OnCandidate) message;
            onCandidate(candidate.getIndex(), candidate.getId(), candidate.getSdp());
        } else if (message instanceof OnOfferNeeded) {
            if (!isReady) {
                stash();
                return;
            }
            OnOfferNeeded offerNeeded = (OnOfferNeeded) message;
            onOfferNeeded(offerNeeded.getSessionId());
        } else if (message instanceof ResetState) {
            if (!isReady) {
                stash();
                return;
            }
            onResetState();
        } else {
            super.onReceive(message);
        }
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof ReplaceStream) {
            if (!isReady) {
                stash();
                return null;
            }
            onReplaceStream(((ReplaceStream) message).getStream());
            return Promise.success(null);
        } else {
            return super.onAsk(message);
        }
    }

    //
    // Inbound Messages
    //

    public static class OnOfferNeeded {

        @Property("nonatomic, readonly")
        private final long sessionId;

        public OnOfferNeeded(long sessionId) {
            this.sessionId = sessionId;
        }

        public long getSessionId() {
            return sessionId;
        }
    }

    public static class OnOffer {

        @Property("nonatomic, readonly")
        private final long sessionId;

        @NotNull
        @Property("nonatomic, readonly")
        private final String sdp;

        public OnOffer(long sessionId, @NotNull String sdp) {
            this.sessionId = sessionId;
            this.sdp = sdp;
        }

        public long getSessionId() {
            return sessionId;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class OnAnswer {

        @Property("nonatomic, readonly")
        private final long sessionId;

        @NotNull
        @Property("nonatomic, readonly")
        private final String sdp;

        public OnAnswer(long sessionId, @NotNull String sdp) {
            this.sdp = sdp;
            this.sessionId = sessionId;
        }

        public long getSessionId() {
            return sessionId;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class OnCandidate {

        @Property("nonatomic, readonly")
        private final long sessionId;
        @Property("nonatomic, readonly")
        private final int index;
        @NotNull
        @Property("nonatomic, readonly")
        private final String sdp;
        @NotNull
        @Property("nonatomic, readonly")
        private final String id;

        public OnCandidate(long sessionId, int index, @NotNull String id, @NotNull String sdp) {
            this.sessionId = sessionId;
            this.index = index;
            this.id = id;
            this.sdp = sdp;
        }

        public long getSessionId() {
            return sessionId;
        }

        public int getIndex() {
            return index;
        }

        @NotNull
        public String getId() {
            return id;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class ResetState {

    }

    public static class ReplaceStream implements AskMessage<Void> {

        private CountedReference<WebRTCMediaStream> stream;

        public ReplaceStream(CountedReference<WebRTCMediaStream> stream) {
            this.stream = stream;
        }

        public CountedReference<WebRTCMediaStream> getStream() {
            return stream;
        }
    }

    private enum PeerConnectionState {
        INITIALIZATION, WAITING_HANDSHAKE, WAITING_ANSWER, READY, CLOSED
    }
}