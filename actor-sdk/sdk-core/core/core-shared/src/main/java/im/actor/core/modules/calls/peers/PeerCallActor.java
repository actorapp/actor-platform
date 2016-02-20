package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiAdvertiseSelf;
import im.actor.core.api.ApiAnswer;
import im.actor.core.api.ApiAnswerCall;
import im.actor.core.api.ApiCallMember;
import im.actor.core.api.ApiCandidate;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiOnAnswer;
import im.actor.core.api.ApiPeerSettings;
import im.actor.core.api.ApiRejectCall;
import im.actor.core.api.ApiSwitchMaster;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class PeerCallActor extends EventBusActor {

    private static final String TAG = "PeerCallActor";

    private final boolean isSlaveMode;

    private PeerNodeSettings selfSettings = new PeerNodeSettings();
    private HashMap<Long, PeerNodeInt> refs = new HashMap<>();

    private boolean isStarted;

    private boolean isMuted = false;
    private WebRTCMediaStream webRTCMediaStream;

    private boolean wasRejected;
    private boolean wasAnswered;
    private boolean haveMaster = false;
    private boolean isAnswered = false;
    private int masterUid;
    private long masterDeviceId;

    public PeerCallActor(boolean isSlaveMode, ModuleContext context) {
        super(context);
        this.isSlaveMode = isSlaveMode;
    }

    public PeerNodeSettings getSelfSettings() {
        return selfSettings;
    }

    public boolean isMuted() {
        return isMuted;
    }

    @Override
    public void preStart() {
        super.preStart();

        WebRTC.getUserAudio().then(new Consumer<WebRTCMediaStream>() {
            @Override
            public void apply(WebRTCMediaStream webRTCMediaStream) {
                PeerCallActor.this.webRTCMediaStream = webRTCMediaStream;
                PeerCallActor.this.webRTCMediaStream.setEnabled(!isMuted);
                for (PeerNodeInt node : refs.values()) {
                    node.setOwnStream(webRTCMediaStream);
                }
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "Unable to load audio");
                self().send(PoisonPill.INSTANCE);
            }
        }).done(self());
    }

    public PeerNodeInt getPeer(final int uid, final long deviceId) {
        if (!refs.containsKey(deviceId)) {
            ActorRef ref = system().actorOf(getPath() + "/" + uid + "/" + deviceId, new ActorCreator() {
                @Override
                public Actor create() {
                    return new PeerNodeActor(uid, deviceId, selfSettings.isPreConnectionEnabled(), self(), context());
                }
            });
            PeerNodeInt peerNodeInt = new PeerNodeInt(ref);
            if (webRTCMediaStream != null) {
                peerNodeInt.setOwnStream(webRTCMediaStream);
            }
            if (isAnswered) {
                peerNodeInt.onAnswered();
            }
            refs.put(deviceId, peerNodeInt);
        }

        return refs.get(deviceId);
    }

    public void startSignaling(String busId) {
        if (!isSlaveMode) {
            throw new RuntimeException("This operation is only for slave mode");
        }
        joinBus(busId);
    }

    public void onMasterSwitched(int uid, long deviceId) {
        if (!isSlaveMode) {
            throw new RuntimeException("This operation is only for slave mode");
        }
        this.masterUid = uid;
        this.masterDeviceId = deviceId;
        this.haveMaster = true;

        if (wasAnswered) {
            wasAnswered = false;
            sendSignaling(uid, deviceId, new ApiAnswerCall());
        }

        if (wasRejected) {
            wasRejected = false;
            sendSignaling(uid, deviceId, new ApiRejectCall());
        }

        sendAdvertise(uid, deviceId, selfSettings.toApi());

        onSignalingStarted();
    }

    public void onMembersReceived(List<ApiCallMember> allMembers) {

    }

    public void onMute(boolean isMuted) {
        this.isMuted = isMuted;
        if (webRTCMediaStream != null) {
            webRTCMediaStream.setEnabled(!isMuted);
        }
    }

    public void onSignalingStarted() {

    }

    public void onSignalingEnded() {

    }

    public void onPeerConnected(int uid, long deviceId) {

    }

    public void onPeerStarted(int uid, long deviceId) {
        if (!isStarted) {
            isStarted = true;
            onFirstPeerStarted();
        }
    }

    public void onFirstPeerStarted() {

    }

    public void onAdvertised(int uid, long deviceId, ApiPeerSettings settings) {

    }

    public void onAnswered(int uid, long deviceId) {

    }

    public void sendAdvertise(int uid, long deviceId, ApiPeerSettings settings) {
        sendSignaling(uid, deviceId, new ApiAdvertiseSelf(settings));
    }

    public void sendNeedOffer(int uid, long deviceId, int destUid, long destDeviceId,
                              ApiPeerSettings settings, boolean isSilent) {
        sendSignaling(uid, deviceId, new ApiNeedOffer(destUid, destDeviceId, settings, isSilent));
    }

    public void sendOnAnswered(int uid, long deviceId, int destUid, long destDeviceId) {
        sendSignaling(uid, deviceId, new ApiOnAnswer(destUid, destDeviceId));
    }

    public void sendAnswer() {
        if (isAnswered) {
            return;
        }
        isAnswered = true;
        if (haveMaster) {
            sendSignaling(masterUid, masterDeviceId, new ApiAnswerCall());
            for (PeerNodeInt node : refs.values()) {
                node.onAnswered();
            }
        } else {
            wasAnswered = true;
        }
    }

    public void sendReject() {
        if (haveMaster) {
            sendSignaling(masterUid, masterDeviceId, new ApiRejectCall());
        } else {
            wasRejected = true;
        }
    }

    public void sendRTCAnswer(int uid, long deviceId, String sdp) {
        sendSignaling(uid, deviceId, new ApiAnswer(0, sdp));
    }

    public void sendRTCOffer(int uid, long deviceId, String sdp) {
        sendSignaling(uid, deviceId, new ApiOffer(0, sdp, getSelfSettings().toApi()));
    }

    public void sendRTCCandidate(int uid, long deviceId, int index, String id, String sdp) {
        sendSignaling(uid, deviceId, new ApiCandidate(0, index, id, sdp));
    }

    public void sendSwitchMaster(int uid, long deviceId) {
        sendSignaling(uid, deviceId, new ApiSwitchMaster());
    }

    public void sendSignaling(int uid, long deviceId, ApiWebRTCSignaling signaling) {
        byte[] msg;
        try {
            msg = signaling.buildContainer();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        sendMessage(uid, deviceId, msg);
    }

    @Override
    public void postStop() {
        super.postStop();
        onSignalingEnded();
        for (PeerNodeInt d : refs.values()) {
            d.stop();
        }
        refs.clear();
    }


    //
    // Messages
    //

    @Override
    public final void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
        if (senderId == null || senderDeviceId == null) {
            return;
        }

        ApiWebRTCSignaling signaling;
        try {
            signaling = ApiWebRTCSignaling.fromBytes(data);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onMessageReceived:ignoring");
            return;
        }

        Log.d(TAG, "onMessageReceived: " + signaling);
        if (signaling instanceof ApiSwitchMaster) {
            onMasterSwitched(senderId, senderDeviceId);
        } else if (signaling instanceof ApiOffer) {
            ApiOffer offer = (ApiOffer) signaling;
            getPeer(senderId, senderDeviceId).onAdvertised(new PeerNodeSettings(offer.getOwnPeerSettings()));
            getPeer(senderId, senderDeviceId).onOffer(offer.getSdp());
        } else if (signaling instanceof ApiAnswer) {
            ApiAnswer answer = (ApiAnswer) signaling;
            getPeer(senderId, senderDeviceId).onAnswer(answer.getSdp());
        } else if (signaling instanceof ApiCandidate) {
            ApiCandidate candidate = (ApiCandidate) signaling;
            getPeer(senderId, senderDeviceId).onCandidate(candidate.getIndex(),
                    candidate.getId(), candidate.getSdp());
        } else if (signaling instanceof ApiNeedOffer) {
            ApiNeedOffer needOffer = (ApiNeedOffer) signaling;
            getPeer(needOffer.getUid(), needOffer.getDevice()).onOfferNeeded();
        } else if (signaling instanceof ApiAdvertiseSelf) {
            ApiAdvertiseSelf advertiseSelf = (ApiAdvertiseSelf) signaling;
            onAdvertised(senderId, senderDeviceId, advertiseSelf.getPeerSettings());
        } else if (signaling instanceof ApiAnswerCall) {
            onAnswered(senderId, senderDeviceId);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof SendAnswer) {
            SendAnswer answer = (SendAnswer) message;
            sendRTCAnswer(answer.getUid(), answer.getDeviceId(), answer.getSdp());
        } else if (message instanceof SendOffer) {
            SendOffer offer = (SendOffer) message;
            sendRTCOffer(offer.getUid(), offer.getDeviceId(), offer.getSdp());
        } else if (message instanceof SendCandidate) {
            SendCandidate candidate = (SendCandidate) message;
            sendRTCCandidate(candidate.getUid(), candidate.getDeviceId(), candidate.getIndex(),
                    candidate.getId(), candidate.getSdp());
        } else if (message instanceof ConnectionCreated) {
            ConnectionCreated connectionCreated = (ConnectionCreated) message;
            onPeerConnected(connectionCreated.getUid(), connectionCreated.getDeviceId());
        } else if (message instanceof ConnectionActive) {
            ConnectionActive connectionActive = (ConnectionActive) message;
            onPeerStarted(connectionActive.getUid(), connectionActive.getDeviceId());
        } else if (message instanceof ChangeMute) {
            onMute(((ChangeMute) message).isMuted());
        } else {
            super.onReceive(message);
        }
    }

    public static class ConnectionCreated {

        private int uid;
        private long deviceId;

        public ConnectionCreated(int uid, long deviceId) {
            this.uid = uid;
            this.deviceId = deviceId;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }
    }

    public static class ConnectionActive {

        private int uid;
        private long deviceId;

        public ConnectionActive(int uid, long deviceId) {
            this.uid = uid;
            this.deviceId = deviceId;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }
    }

    public static class SendAnswer {
        private int uid;
        private long deviceId;
        private String sdp;

        public SendAnswer(int uid, long deviceId, String sdp) {
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

        public String getSdp() {
            return sdp;
        }
    }

    public static class SendOffer {

        private int uid;
        private long deviceId;
        private String sdp;

        public SendOffer(int uid, long deviceId, String sdp) {
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

        public String getSdp() {
            return sdp;
        }
    }

    public static class SendCandidate {

        private int uid;
        private long deviceId;
        private int index;
        private String id;
        private String sdp;

        public SendCandidate(int uid, long deviceId, int index, String id, String sdp) {
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

    public static class ChangeMute {
        private boolean isMuted;

        public ChangeMute(boolean isMuted) {
            this.isMuted = isMuted;
        }

        public boolean isMuted() {
            return isMuted;
        }
    }
}