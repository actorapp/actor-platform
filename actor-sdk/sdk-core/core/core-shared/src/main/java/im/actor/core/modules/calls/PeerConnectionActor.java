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
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.FunctionTupled2;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.webrtc.WebRTCLocalStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCPeerConnectionCallback;
import im.actor.runtime.webrtc.WebRTCRemoteStream;
import im.actor.runtime.webrtc.WebRTCSessionDescription;

public class PeerConnectionActor extends ModuleActor {

    @NotNull
    public static ActorCreator CONSTRUCTOR(@NotNull final ActorRef root,
                                           final int uid,
                                           final long deviceId,
                                           @NotNull final ModuleContext context) {
        return new ActorCreator() {
            @Override
            public Actor create() {
                return new PeerConnectionActor(root, uid, deviceId, context);
            }
        };
    }

    private final String TAG;
    @NotNull
    private final ActorRef root;
    private final int uid;
    private final long deviceId;
    private boolean isReady = false;
    private boolean isReadyForCandidates = false;
    @NotNull
    private WebRTCPeerConnection peerConnection;
    @NotNull
    private State state = State.INITIALIZATION;

    public PeerConnectionActor(@NotNull ActorRef root, int uid, long deviceId, @NotNull ModuleContext context) {
        super(context);
        TAG = "PeerConnection#" + uid + "(" + deviceId + ")";
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
        Promises.tuple(WebRTC.createPeerConnection(), WebRTC.getUserAudio()).map(new FunctionTupled2<WebRTCPeerConnection, WebRTCLocalStream, WebRTCPeerConnection>() {
            @Override
            public WebRTCPeerConnection apply(WebRTCPeerConnection webRTCPeerConnection, WebRTCLocalStream stream) {
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
                    public void onCandidatesEnd() {

                    }

                    @Override
                    public void onStreamAdded(WebRTCRemoteStream stream) {

                    }

                    @Override
                    public void onStreamRemoved(WebRTCRemoteStream stream) {

                    }
                });
                state = State.WAITING_HANDSHAKE;
                isReady = true;
                unstashAll();
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                e.printStackTrace();
                // TODO: Handle It

                Log.d(TAG, "preStart:error");
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
        peerConnection.createOffer().mapPromise(new Function<WebRTCSessionDescription, Promise<WebRTCSessionDescription>>() {
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
                // TODO: Handle It
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
        }).mapPromise(new Function<WebRTCSessionDescription, Promise<WebRTCSessionDescription>>() {
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
                e.printStackTrace();
                // TODO: Handle It
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
                // TODO: Handle It
            }
        }).done(self());
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

    private enum State {
        INITIALIZATION, WAITING_HANDSHAKE, WAITING_ANSWER, READY
    }
}