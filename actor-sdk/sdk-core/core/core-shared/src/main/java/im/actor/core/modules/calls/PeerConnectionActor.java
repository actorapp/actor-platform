package im.actor.core.modules.calls;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

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
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCPeerConnectionCallback;
import im.actor.runtime.webrtc.WebRTCSessionDescription;
import im.actor.runtime.webrtc.sdp.SDP;
import im.actor.runtime.webrtc.sdp.SDPScheme;
import im.actor.runtime.webrtc.sdp.entities.SDPCodec;
import im.actor.runtime.webrtc.sdp.entities.SDPMedia;
import im.actor.runtime.webrtc.sdp.entities.SDPMediaMode;

public class PeerConnectionActor extends ModuleActor {

    @NotNull
    public static ActorCreator CONSTRUCTOR(@NotNull final ActorRef root,
                                           final int uid,
                                           final long deviceId,
                                           final boolean isMuted,
                                           @NotNull final ModuleContext context) {
        return new ActorCreator() {
            @Override
            public Actor create() {
                return new PeerConnectionActor(root, uid, deviceId, isMuted, context);
            }
        };
    }

    private final String TAG;
    @NotNull
    private final ActorRef root;
    private final int uid;
    private final long deviceId;
    private boolean isMuted;
    private boolean isReady = false;
    private boolean isReadyForCandidates = false;
    @NotNull
    private WebRTCPeerConnection peerConnection;
    @NotNull
    private WebRTCMediaStream stream;
    @NotNull
    private State state = State.INITIALIZATION;

    public PeerConnectionActor(@NotNull ActorRef root, int uid, long deviceId, boolean isMuted, @NotNull ModuleContext context) {
        super(context);
        TAG = "PeerConnection#" + uid + "(" + deviceId + ")";
        this.isMuted = isMuted;
        this.root = root;
        this.uid = uid;
        this.deviceId = deviceId;
    }

    public int getUid() {
        return uid;
    }

    public long getDeviceId() {
        return deviceId;
    }

    @Override
    public void preStart() {

        Log.d(TAG, "preStart");

        isReady = false;
        Promises.tuple(WebRTC.createPeerConnection(), WebRTC.getUserAudio()).map(new FunctionTupled2<WebRTCPeerConnection, WebRTCMediaStream, WebRTCPeerConnection>() {
            @Override
            public WebRTCPeerConnection apply(WebRTCPeerConnection webRTCPeerConnection, WebRTCMediaStream stream) {
                stream.setEnabled(!isMuted);
                PeerConnectionActor.this.stream = stream;
                webRTCPeerConnection.addOwnStream(stream);
                return webRTCPeerConnection;
            }
        }).then(new Consumer<WebRTCPeerConnection>() {
            @Override
            public void apply(WebRTCPeerConnection webRTCPeerConnection) {
                Log.d(TAG, "preStart:then");
                PeerConnectionActor.this.peerConnection = webRTCPeerConnection;
                PeerConnectionActor.this.peerConnection.addCallback(new WebRTCPeerConnectionCallback() {
                    @Override
                    public void onCandidate(int label, String id, String candidate) {
                        root.send(new DoCandidate(uid, deviceId, label, id, candidate));
                    }

                    @Override
                    public void onStreamAdded(WebRTCMediaStream stream) {
                        root.send(new OnStreamAdded(uid, deviceId, stream));
                    }

                    @Override
                    public void onStreamRemoved(WebRTCMediaStream stream) {
                        root.send(new OnStreamRemoved(uid, deviceId, stream));
                    }

                    @Override
                    public void onRenegotiationNeeded() {

                    }
                });
                state = State.WAITING_HANDSHAKE;
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
        if (state != State.WAITING_HANDSHAKE) {
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
                root.send(new DoOffer(uid, deviceId, description.getSdp()));
                state = State.WAITING_ANSWER;
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
        if (state != State.WAITING_HANDSHAKE) {
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
                root.send(new DoAnswer(uid, deviceId, description.getSdp()));
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
        if (state != State.WAITING_ANSWER) {
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
        state = State.CLOSED;
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        if (stream != null) {
            stream.close();
            stream = null;
        }
        self().send(PoisonPill.INSTANCE);

        // TODO: Notify Root
    }

    private void onHandShakeCompleted() {
        isReady = true;
        isReadyForCandidates = true;
        state = State.READY;
        unstashAll();
    }

    public void onCandidate(int index, @NotNull String id, @NotNull String sdp) {
        peerConnection.addCandidate(index, id, sdp);
    }

    public void onMute() {
        if (stream != null) {
            stream.setEnabled(false);
        }
    }

    public void onUnmute() {
        if (stream != null) {
            stream.setEnabled(true);
        }
    }

    public void onEnded() {
        peerConnection.close();
        stream.close();
        isReady = false;
        state = State.CLOSED;
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
        } else if (message instanceof DoMute) {
            if (!isReady) {
                stash();
                return;
            }
            onMute();
        } else if (message instanceof DoUnmute) {
            if (!isReady) {
                stash();
                return;
            }
            onUnmute();
        } else {
            super.onReceive(message);
        }
    }

    //
    // Outbound Messages
    //

    public static class DoOffer {

        @Property("nonatomic, readonly")
        private int uid;
        @Property("nonatomic, readonly")
        private long deviceId;
        @NotNull
        @Property("nonatomic, readonly")
        private String sdp;

        public DoOffer(int uid, long deviceId, @NotNull String sdp) {
            this.uid = uid;
            this.deviceId = deviceId;
            this.sdp = sdp;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class DoAnswer {

        @Property("nonatomic, readonly")
        private final int uid;
        @Property("nonatomic, readonly")
        private final long deviceId;
        @NotNull
        @Property("nonatomic, readonly")
        private final String sdp;

        public DoAnswer(int uid, long deviceId, @NotNull String sdp) {
            this.uid = uid;
            this.deviceId = deviceId;
            this.sdp = sdp;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class DoCandidate {

        private int uid;
        private long deviceId;
        private int index;
        private String id;
        private String sdp;

        public DoCandidate(int uid, long deviceId, int index, String id, String sdp) {
            this.uid = uid;
            this.deviceId = deviceId;
            this.index = index;
            this.id = id;
            this.sdp = sdp;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }

        public int getIndex() {
            return index;
        }

        public String getId() {
            return id;
        }

        public String getSdp() {
            return sdp;
        }
    }

    public static class OnStreamAdded {
        private int uid;
        private long deviceId;
        private WebRTCMediaStream stream;

        public OnStreamAdded(int uid, long deviceId, WebRTCMediaStream stream) {
            this.uid = uid;
            this.deviceId = deviceId;
            this.stream = stream;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }

        public WebRTCMediaStream getStream() {
            return stream;
        }
    }

    public static class OnStreamRemoved {

        private int uid;
        private long deviceId;
        private WebRTCMediaStream stream;

        public OnStreamRemoved(int uid, long deviceId, WebRTCMediaStream stream) {
            this.uid = uid;
            this.deviceId = deviceId;
            this.stream = stream;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }

        public WebRTCMediaStream getStream() {
            return stream;
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

    public static class DoMute {

    }

    public static class DoUnmute {

    }

    private enum State {
        INITIALIZATION, WAITING_HANDSHAKE, WAITING_ANSWER, READY, CLOSED
    }
}