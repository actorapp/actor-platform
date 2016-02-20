package im.actor.core.modules.calls.peers;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import im.actor.core.modules.ModuleContext;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.FunctionTupled2;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCPeerConnectionCallback;
import im.actor.runtime.webrtc.WebRTCSessionDescription;
import im.actor.runtime.webrtc.WebRTCSettings;

public class PeerConnectionActor extends ModuleActor {

    @NotNull
    public static ActorCreator CONSTRUCTOR(@NotNull final WebRTCMediaStream mediaStream,
                                           final boolean isEnabled,
                                           @NotNull final ActorRef nodeActor,
                                           @NotNull final ModuleContext context) {
        return new ActorCreator() {
            @Override
            public Actor create() {
                return new PeerConnectionActor(mediaStream, isEnabled, nodeActor, context);
            }
        };
    }


    private final String TAG;
    @NotNull
    private final ActorRef nodeActor;
    @NotNull
    private final WebRTCMediaStream stream;

    @NotNull
    private WebRTCPeerConnection peerConnection;

    private boolean isEnabled;
    private boolean isReady = false;
    private boolean isReadyForCandidates = false;
    @NotNull
    private PeerConnectionState state = PeerConnectionState.INITIALIZATION;

    private ArrayList<WebRTCMediaStream> incomingStreams = new ArrayList<>();

    public PeerConnectionActor(@NotNull WebRTCMediaStream mediaStream,
                               boolean isEnabled,
                               @NotNull ActorRef nodeActor,
                               @NotNull ModuleContext context) {
        super(context);
        TAG = "PeerConnection";
        this.nodeActor = nodeActor;
        this.isEnabled = isEnabled;
        this.stream = mediaStream;
    }

    @Override
    public void preStart() {

        Log.d(TAG, "preStart");

        isReady = false;

        WebRTCIceServer[] iceServers = config().getWebRTCIceServers();
        WebRTCSettings settings = new WebRTCSettings(false, false);
        WebRTC.createPeerConnection(iceServers, settings).then(new Consumer<WebRTCPeerConnection>() {
            @Override
            public void apply(WebRTCPeerConnection webRTCPeerConnection) {
                Log.d(TAG, "preStart:then");
                PeerConnectionActor.this.peerConnection = webRTCPeerConnection;
                PeerConnectionActor.this.peerConnection.addOwnStream(stream);
                PeerConnectionActor.this.peerConnection.addCallback(new WebRTCPeerConnectionCallback() {
                    @Override
                    public void onCandidate(int label, String id, String candidate) {
                        nodeActor.send(new PeerNodeActor.DoCandidate(label, id, candidate));
                    }

                    @Override
                    public void onStreamAdded(WebRTCMediaStream stream) {
                        stream.setEnabled(isEnabled);
                        incomingStreams.add(stream);
                        nodeActor.send(new PeerNodeActor.OnStreamAdded());
                    }

                    @Override
                    public void onStreamRemoved(WebRTCMediaStream stream) {
                        incomingStreams.remove(stream);
                        nodeActor.send(new PeerNodeActor.OnStreamRemoved());
                        // root.send(new OnStreamRemoved(uid, deviceId, stream));
                    }

                    @Override
                    public void onRenegotiationNeeded() {

                    }
                });
                state = PeerConnectionState.WAITING_HANDSHAKE;
                isReady = true;
                unstashAll();
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "preStart:error");
                e.printStackTrace();
                onHandshakeFailure();
            }
        }).done(self());
    }

    public void onOfferNeeded() {
        // Ignore if we are not waiting for handshake
        if (state != PeerConnectionState.WAITING_HANDSHAKE) {
            return;
        }

        //
        // Stages
        // 1. Create Offer
        // 2. Set Local Description
        // 3. Send Offer
        //

        Log.d(TAG, "onOfferNeeded");
        isReady = false;
        peerConnection.createOffer().map(OPTIMIZE_SDP).mapPromise(new Function<WebRTCSessionDescription, Promise<WebRTCSessionDescription>>() {
            @Override
            public Promise<WebRTCSessionDescription> apply(WebRTCSessionDescription description) {
                return peerConnection.setLocalDescription(description);
            }
        }).then(new Consumer<WebRTCSessionDescription>() {
            @Override
            public void apply(WebRTCSessionDescription description) {
                Log.d(TAG, "onOfferNeeded:then");
                nodeActor.send(new PeerNodeActor.DoOffer(description.getSdp()));
                state = PeerConnectionState.WAITING_ANSWER;
                isReady = true;
                unstashAll();
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "onOfferNeeded:failure");
                e.printStackTrace();
                onHandshakeFailure();
            }
        }).done(self());
    }

    public void onOffer(@NotNull String sdp) {
        // Ignore if we are not waiting for handshake
        if (state != PeerConnectionState.WAITING_HANDSHAKE) {
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

        Log.d(TAG, "Received Offer");

        isReady = false;
        peerConnection.setRemoteDescription(new WebRTCSessionDescription("offer", sdp)).mapPromise(new Function<WebRTCSessionDescription, Promise<WebRTCSessionDescription>>() {
            @Override
            public Promise<WebRTCSessionDescription> apply(WebRTCSessionDescription description) {
                return peerConnection.createAnswer();
            }
        }).map(OPTIMIZE_SDP).mapPromise(new Function<WebRTCSessionDescription, Promise<WebRTCSessionDescription>>() {
            @Override
            public Promise<WebRTCSessionDescription> apply(WebRTCSessionDescription description) {
                return peerConnection.setLocalDescription(description);
            }
        }).then(new Consumer<WebRTCSessionDescription>() {
            @Override
            public void apply(WebRTCSessionDescription description) {
                nodeActor.send(new PeerNodeActor.DoAnswer(description.getSdp()));
                onHandShakeCompleted();
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "onOffer:failure");
                e.printStackTrace();
                onHandshakeFailure();
            }
        }).done(self());
    }

    public void onAnswer(@NotNull String sdp) {
        // Ignore if we are not waiting for answer
        if (state != PeerConnectionState.WAITING_ANSWER) {
            return;
        }

        Log.d(TAG, "Received Answer");

        //
        // Stages
        // 1. Set Remote Description
        // 2. Enter READY mode
        //
        Log.d(TAG, "onAnswer");
        peerConnection.setRemoteDescription(new WebRTCSessionDescription("answer", sdp)).then(new Consumer<WebRTCSessionDescription>() {
            @Override
            public void apply(WebRTCSessionDescription description) {
                Log.d(TAG, "onAnswer:then");
                onHandShakeCompleted();
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "onAnswer:failure");
                e.printStackTrace();
                onHandshakeFailure();
            }
        }).done(self());
    }

    private void onHandshakeFailure() {
        isReady = false;
        isReadyForCandidates = false;
        state = PeerConnectionState.CLOSED;
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        self().send(PoisonPill.INSTANCE);

        // TODO: Notify Root
    }

    private void onHandShakeCompleted() {
        isReady = true;
        isReadyForCandidates = true;
        state = PeerConnectionState.READY;
        unstashAll();
    }

    public void onCandidate(int index, @NotNull String id, @NotNull String sdp) {
        peerConnection.addCandidate(index, id, sdp);
    }

    public void onEnabled(boolean isEnabled) {
        if (isEnabled == this.isEnabled) {
            return;
        }
        this.isEnabled = isEnabled;
        for (WebRTCMediaStream s : incomingStreams) {
            s.setEnabled(isEnabled);
        }
    }

    public void onEnded() {
        Log.d(TAG, "OnEnded");
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        isReady = false;
        state = PeerConnectionState.CLOSED;
        self().send(PoisonPill.INSTANCE);
    }

    //
    // Configuration
    //

    private static Function<WebRTCSessionDescription, WebRTCSessionDescription> OPTIMIZE_SDP
            = new Function<WebRTCSessionDescription, WebRTCSessionDescription>() {
        @Override
        public WebRTCSessionDescription apply(WebRTCSessionDescription description) {
//            SDPScheme sdpScheme = SDP.parse(description.getSdp());
//
//            for (SDPMedia m : sdpScheme.getMediaLevel()) {
//
//                // Disabling media streams
//                // m.setMode(SDPMediaMode.INACTIVE);
//
//                // Optimizing opus
//                if ("audio".equals(m.getType())) {
//                    for (SDPCodec codec : m.getCodecs()) {
//                        if ("opus".equals(codec.getName())) {
//                            codec.getFormat().put("maxcodedaudiobandwidth", "16000");
//                            codec.getFormat().put("maxaveragebitrate", "20000");
//                            codec.getFormat().put("stereo", "0");
//                            codec.getFormat().put("useinbandfec", "1");
//                            codec.getFormat().put("usedtx", "1");
//                        }
//                    }
//                }
//            }
//
//            return new WebRTCSessionDescription(description.getType(), sdpScheme.toSDP());
            return description;
        }
    };

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
            onOffer(((OnOffer) message).getSdp());
        } else if (message instanceof OnAnswer) {
            if (!isReady) {
                stash();
                return;
            }
            onAnswer(((OnAnswer) message).getSdp());
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
            onOfferNeeded();
        } else if (message instanceof DoStop) {
            if (!isReady) {
                stash();
                return;
            }
            onEnded();
        } else if (message instanceof DoEnable) {
            if (!isReady) {
                stash();
                return;
            }
            onEnabled(((DoEnable) message).isEnabled());
        } else {
            super.onReceive(message);
        }
    }

    //
    // Inbound Messages
    //

    public static class OnOfferNeeded {

    }

    public static class OnOffer {

        @NotNull
        @Property("nonatomic, readonly")
        private String sdp;

        public OnOffer(@NotNull String sdp) {
            this.sdp = sdp;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class OnAnswer {

        @NotNull
        @Property("nonatomic, readonly")
        private String sdp;

        public OnAnswer(@NotNull String sdp) {
            this.sdp = sdp;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class OnCandidate {

        @Property("nonatomic, readonly")
        private final int index;
        @NotNull
        @Property("nonatomic, readonly")
        private final String sdp;
        @NotNull
        @Property("nonatomic, readonly")
        private final String id;

        public OnCandidate(int index, @NotNull String id, @NotNull String sdp) {
            this.index = index;
            this.id = id;
            this.sdp = sdp;
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

    public static class DoStop {

    }

    public static class DoEnable {

        boolean isEnabled;

        public DoEnable(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        public boolean isEnabled() {
            return isEnabled;
        }
    }
}