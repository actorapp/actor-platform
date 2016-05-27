package im.actor.core.modules.calls.peers;

import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiICEServer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.messages.RTCAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCAnswer;
import im.actor.core.modules.calls.peers.messages.RTCCandidate;
import im.actor.core.modules.calls.peers.messages.RTCCloseSession;
import im.actor.core.modules.calls.peers.messages.RTCDispose;
import im.actor.core.modules.calls.peers.messages.RTCMasterAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class PeerCallActor extends ModuleActor {

    private static final String TAG = "PeerCallActor";

    // Parent Actor for handling events
    private final PeerCallCallback callback;

    // Peer Settings
    private final PeerSettings selfSettings;

    // WebRTC objects
    private List<ApiICEServer> iceServers;
    private HashMap<Long, PeerNodeInt> refs = new HashMap<>();
    private WebRTCMediaStream webRTCMediaStream;

    // State objects
    private boolean isOwnStarted = false;
    private boolean isMuted = false;
    private boolean isOwnMediaStarted = false;
    private boolean videoEnabled = true;

    public PeerCallActor(PeerCallCallback callback, PeerSettings selfSettings, ModuleContext context) {
        super(context);
        this.callback = callback;
        this.selfSettings = selfSettings;
    }

    @Override
    public void preStart() {
        super.preStart();

//        if (selfSettings.isPreConnectionEnabled()) {
//
//        }
//
//        WebRTC.getUserMedia().then(new Consumer<WebRTCMediaStream>() {
//            @Override
//            public void apply(WebRTCMediaStream webRTCMediaStream) {
//                PeerCallActor.this.webRTCMediaStream = webRTCMediaStream;
//                PeerCallActor.this.webRTCMediaStream.setEnabled(isOwnStarted && !isMuted);
//                for (PeerNodeInt node : refs.values()) {
//                    node.setOwnStream(webRTCMediaStream);
//                }
//            }
//        }).failure(new Consumer<Exception>() {
//            @Override
//            public void apply(Exception e) {
//                Log.d(TAG, "Unable to load audio");
//                self().send(PoisonPill.INSTANCE);
//            }
//        }).done(self());
    }

    //
    // Media Settings
    //

    public void onMuteChanged(boolean isMuted) {
        this.isMuted = isMuted;
        if (webRTCMediaStream != null) {
            webRTCMediaStream.setAudioEnabled(isOwnStarted && !this.isMuted);
        }
    }

    public void onVideoEnabled(boolean enabled) {
        this.videoEnabled = enabled;
        if (webRTCMediaStream != null) {
            webRTCMediaStream.setVideoEnabled(isOwnStarted && this.videoEnabled);
        }
    }

    public void onOwnStarted() {
        if (isOwnStarted) {
            return;
        }
        isOwnStarted = true;

        WebRTC.getUserMedia(config().isVideoCallsEnabled()).then(webRTCMediaStream1 -> {
            PeerCallActor.this.webRTCMediaStream = webRTCMediaStream1;
            PeerCallActor.this.webRTCMediaStream.setAudioEnabled(!isMuted);
            PeerCallActor.this.webRTCMediaStream.setVideoEnabled(videoEnabled);
            for (PeerNodeInt node : refs.values()) {
                node.setOwnStream(webRTCMediaStream1);
            }
        }).failure(e -> {
            Log.d(TAG, "Unable to load audio");
            self().send(PoisonPill.INSTANCE);
        });
//
//        if (webRTCMediaStream != null) {
//            webRTCMediaStream.setEnabled(!isMuted);
//        }
    }

    public void onMasterAdvertised(List<ApiICEServer> iceServers) {
        if (this.iceServers == null) {
            this.iceServers = iceServers;
            for (PeerNodeInt node : refs.values()) {
                node.onAdvertisedMaster(iceServers);
            }
        }
    }

    //
    // Peer Collection
    //

    public PeerNodeInt getPeer(long deviceId) {
        if (!refs.containsKey(deviceId)) {
            return createNewPeer(deviceId);
        }
        return refs.get(deviceId);
    }

    public PeerNodeInt createNewPeer(final long deviceId) {
        PeerNodeInt peerNodeInt = new PeerNodeInt(deviceId, new NodeCallback(),
                selfSettings, self(), context());
        if (webRTCMediaStream != null) {
            peerNodeInt.setOwnStream(webRTCMediaStream);
        }
        if (this.iceServers != null) {
            peerNodeInt.onAdvertisedMaster(iceServers);
        }
        refs.put(deviceId, peerNodeInt);
        return peerNodeInt;
    }

    public void disposePeer(long deviceId) {
        PeerNodeInt peerNodeInt = refs.remove(deviceId);
        if (peerNodeInt != null) {
            peerNodeInt.kill();
        }
    }


    @Override
    public void postStop() {
        super.postStop();
        for (PeerNodeInt d : refs.values()) {
            d.kill();
        }
        refs.clear();
        if (webRTCMediaStream != null) {
            webRTCMediaStream.setAudioEnabled(false);
            webRTCMediaStream.close();
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof RTCStart) {
            RTCStart start = (RTCStart) message;
            getPeer(start.getDeviceId()).startConnection();
        } else if (message instanceof RTCDispose) {
            RTCDispose dispose = (RTCDispose) message;
            disposePeer(dispose.getDeviceId());
        } else if (message instanceof RTCOffer) {
            RTCOffer offer = (RTCOffer) message;
            getPeer(offer.getDeviceId()).onOffer(offer.getSessionId(), offer.getSdp());
        } else if (message instanceof RTCAnswer) {
            RTCAnswer answer = (RTCAnswer) message;
            getPeer(answer.getDeviceId()).onAnswer(answer.getSessionId(), answer.getSdp());
        } else if (message instanceof RTCCandidate) {
            RTCCandidate candidate = (RTCCandidate) message;
            getPeer(candidate.getDeviceId()).onCandidate(candidate.getMdpIndex(),
                    candidate.getId(), candidate.getSdp());
        } else if (message instanceof RTCNeedOffer) {
            RTCNeedOffer needOffer = (RTCNeedOffer) message;
            getPeer(needOffer.getDeviceId()).onOfferNeeded(needOffer.getSessionId());
        } else if (message instanceof RTCAdvertised) {
            RTCAdvertised advertised = (RTCAdvertised) message;
            getPeer(advertised.getDeviceId()).onAdvertised(advertised.getSettings());
        } else if (message instanceof RTCCloseSession) {
            RTCCloseSession closeSession = (RTCCloseSession) message;
            getPeer(closeSession.getDeviceId()).closeSession(closeSession.getSessionId());
        } else if (message instanceof RTCMasterAdvertised) {
            RTCMasterAdvertised masterAdvertised = (RTCMasterAdvertised) message;
            onMasterAdvertised(masterAdvertised.getIceServers());
        } else if (message instanceof MuteChanged) {
            MuteChanged muteChanged = (MuteChanged) message;
            onMuteChanged(muteChanged.isMuted());
        } else if (message instanceof VideoEnabled) {
            VideoEnabled videoEnabled = (VideoEnabled) message;
            onVideoEnabled(videoEnabled.isEnabled());
        } else if (message instanceof OwnStarted) {
            onOwnStarted();
        } else {
            super.onReceive(message);
        }
    }

    public static class MuteChanged {
        private boolean isMuted;

        public MuteChanged(boolean isMuted) {
            this.isMuted = isMuted;
        }

        public boolean isMuted() {
            return isMuted;
        }
    }

    public static class VideoEnabled {

        private boolean enabled;

        public VideoEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static class OwnStarted {

    }

    public static class MasterAdvertised {

    }

    private class NodeCallback implements PeerNodeCallback {

        @Override
        public void onOffer(long deviceId, long sessionId, String sdp) {
            callback.onOffer(deviceId, sessionId, sdp);
        }

        @Override
        public void onAnswer(long deviceId, long sessionId, String sdp) {
            callback.onAnswer(deviceId, sessionId, sdp);
        }

        @Override
        public void onNegotiationSuccessful(long deviceId, long sessionId) {
            callback.onNegotiationSuccessful(deviceId, sessionId);
        }

        @Override
        public void onCandidate(long deviceId, int mdpIndex, String id, String sdp) {
            callback.onCandidate(deviceId, mdpIndex, id, sdp);
        }

        @Override
        public void onPeerStateChanged(long deviceId, PeerState state) {
            callback.onPeerStateChanged(deviceId, state);
        }

        @Override
        public void onStreamAdded(long deviceId, WebRTCMediaStream stream) {
            callback.onStreamAdded(deviceId, stream);
        }

        @Override
        public void onStreamRemoved(long deviceId, WebRTCMediaStream stream) {
            callback.onStreamRemoved(deviceId, stream);
        }

        @Override
        public void onPeerConnectionCreated(WebRTCPeerConnection peerConnection) {
            callback.onPeerConnectionCreated(peerConnection);
        }
    }
}