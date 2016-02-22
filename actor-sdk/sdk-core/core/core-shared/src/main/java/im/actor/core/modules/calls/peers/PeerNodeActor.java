package im.actor.core.modules.calls.peers;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

import im.actor.core.modules.ModuleContext;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSupervisor;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.annotations.ActorMessage;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class PeerNodeActor extends ModuleActor {

    private static final int STASH_CONNECTION = 1;

    private final long deviceId;
    private final int uid;
    private final boolean isPreConnectEnabled;
    private final ActorRef callActor;
    private ActorRef peerConnection;
    private PeerNodeSettings settings;
    private WebRTCMediaStream mediaStream;
    private boolean isAnswered = false;
    private boolean isMuted = false;
    private boolean isConnected = false;

    public PeerNodeActor(int uid, long deviceId, boolean isPreConnectEnabled, ActorRef callActor, ModuleContext context) {
        super(context);
        this.isPreConnectEnabled = isPreConnectEnabled;
        this.callActor = callActor;
        this.uid = uid;
        this.deviceId = deviceId;
    }

    @Override
    public void preStart() {

    }

    @ActorMessage
    public void onAdvertised(PeerNodeSettings settings) {
        this.settings = settings;
        makePeerConnectionIfNeeded();
        enablePeerConnectionIfNeeded();
    }

    @ActorMessage
    public void onAnswered() {
        this.isAnswered = true;
        if (mediaStream != null) {
            this.mediaStream.setEnabled(!isMuted);
        }
        makePeerConnectionIfNeeded();
        enablePeerConnectionIfNeeded();
        if (isConnected) {
            callActor.send(new PeerCallActor.ConnectionActive(uid, deviceId));
        }
    }

    @ActorMessage
    public void setOwnSetStream(WebRTCMediaStream mediaStream) {

        this.mediaStream = mediaStream;
        this.mediaStream.setEnabled(!isMuted && isAnswered);

        makePeerConnectionIfNeeded();
        enablePeerConnectionIfNeeded();
    }

    private void makePeerConnectionIfNeeded() {
        if (peerConnection == null
                && settings != null
                && mediaStream != null
                && ((settings.isPreConnectionEnabled() && isPreConnectEnabled) || isAnswered)) {

            peerConnection = system().actorOf(getPath() + "/connection",
                    PeerConnectionActor.CONSTRUCTOR(mediaStream, isAnswered, self(), context()),
                    new ActorSupervisor() {
                        @Override
                        public void onActorStopped(ActorRef ref) {
                            self().send(PoisonPill.INSTANCE);
                        }
                    });
            unstashAll(STASH_CONNECTION);
            callActor.send(new PeerCallActor.ConnectionCreated(uid, deviceId));
        }
    }

    private void enablePeerConnectionIfNeeded() {
        if (peerConnection == null) {
            return;
        }
        if (!isAnswered) {
            return;
        }

        peerConnection.send(new PeerConnectionActor.DoEnable(true));
    }

    @ActorMessage
    public void onStreamAdded() {
        if (isConnected) {
            return;
        }
        isConnected = true;
        if (isAnswered) {
            callActor.send(new PeerCallActor.ConnectionActive(uid, deviceId));
        }
    }

    @ActorMessage
    public void onOfferNeeded() {
        peerConnection.send(new PeerConnectionActor.OnOfferNeeded());
    }

    @Override
    public void postStop() {
        if (peerConnection != null) {
            peerConnection.send(PoisonPill.INSTANCE);
            peerConnection = null;
        }
    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnOfferReceived) {
            if (peerConnection == null) {
                stash(STASH_CONNECTION);
                return;
            }
            OnOfferReceived offer = (OnOfferReceived) message;
            peerConnection.send(new PeerConnectionActor.OnOffer(offer.getSdp()));
        } else if (message instanceof DoOffer) {
            DoOffer offer = (DoOffer) message;
            callActor.send(new PeerCallActor.SendOffer(uid, deviceId, offer.getSdp()));
        } else if (message instanceof OnAnswerReceived) {
            if (peerConnection == null) {
                stash(STASH_CONNECTION);
                return;
            }
            OnAnswerReceived answer = (OnAnswerReceived) message;
            peerConnection.send(new PeerConnectionActor.OnAnswer(answer.getSdp()));
        } else if (message instanceof DoAnswer) {
            DoAnswer answer = (DoAnswer) message;
            callActor.send(new PeerCallActor.SendAnswer(uid, deviceId, answer.getSdp()));
        } else if (message instanceof OnCandidateReceived) {
            if (peerConnection == null) {
                stash(STASH_CONNECTION);
                return;
            }
            OnCandidateReceived candidate = (OnCandidateReceived) message;
            peerConnection.send(new PeerConnectionActor.OnCandidate(candidate.getIndex(),
                    candidate.getId(), candidate.getSdp()));
        } else if (message instanceof DoCandidate) {
            DoCandidate candidate = (DoCandidate) message;
            callActor.send(new PeerCallActor.SendCandidate(uid, deviceId, candidate.getIndex(),
                    candidate.getId(), candidate.getSdp()));
        } else if (message instanceof OnOfferNeeded) {
            if (peerConnection == null) {
                stash(STASH_CONNECTION);
                return;
            }
            onOfferNeeded();
        } else if (message instanceof OnAdvertised) {
            OnAdvertised nodeSettings = (OnAdvertised) message;
            onAdvertised(nodeSettings.getSettings());
        } else if (message instanceof OnAnswered) {
            onAnswered();
        } else if (message instanceof OnStreamAdded) {
            onStreamAdded();
        } else if (message instanceof OnStreamRemoved) {
            // Do nothing
        } else if (message instanceof SetOwnStream) {
            setOwnSetStream(((SetOwnStream) message).getMediaStream());
        } else {
            super.onReceive(message);
        }
    }

    public static class OnAdvertised {

        private PeerNodeSettings settings;

        public OnAdvertised(PeerNodeSettings settings) {
            this.settings = settings;
        }

        public PeerNodeSettings getSettings() {
            return settings;
        }
    }

    public static class OnAnswered {

    }

    public static class OnOfferNeeded {

    }

    public static class OnOfferReceived {

        @NotNull
        @Property("nonatomic, readonly")
        private String sdp;

        public OnOfferReceived(@NotNull String sdp) {
            this.sdp = sdp;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class OnAnswerReceived {

        @NotNull
        @Property("nonatomic, readonly")
        private String sdp;

        public OnAnswerReceived(@NotNull String sdp) {
            this.sdp = sdp;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class OnCandidateReceived {

        @Property("nonatomic, readonly")
        private final int index;
        @NotNull
        @Property("nonatomic, readonly")
        private final String id;
        @NotNull
        @Property("nonatomic, readonly")
        private final String sdp;

        public OnCandidateReceived(int index, @NotNull String id, @NotNull String sdp) {
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


    public static class DoOffer {

        @NotNull
        @Property("nonatomic, readonly")
        private String sdp;

        public DoOffer(@NotNull String sdp) {
            this.sdp = sdp;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class DoAnswer {

        @NotNull
        @Property("nonatomic, readonly")
        private String sdp;

        public DoAnswer(@NotNull String sdp) {
            this.sdp = sdp;
        }

        @NotNull
        public String getSdp() {
            return sdp;
        }
    }

    public static class DoCandidate {

        @Property("nonatomic, readonly")
        private final int index;
        @NotNull
        @Property("nonatomic, readonly")
        private final String id;
        @NotNull
        @Property("nonatomic, readonly")
        private final String sdp;

        public DoCandidate(int index, @NotNull String id, @NotNull String sdp) {
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


    public static class OnStreamAdded {

    }

    public static class OnStreamRemoved {

    }

    public static class SetOwnStream {

        private WebRTCMediaStream mediaStream;

        public SetOwnStream(WebRTCMediaStream mediaStream) {
            this.mediaStream = mediaStream;
        }

        public WebRTCMediaStream getMediaStream() {
            return mediaStream;
        }
    }
}