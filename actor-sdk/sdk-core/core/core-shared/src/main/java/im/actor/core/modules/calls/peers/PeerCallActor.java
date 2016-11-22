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
import im.actor.core.modules.calls.peers.messages.RTCMediaStateUpdated;
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.CountedReference;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;

/**
 * Manages Connections to all Nodes
 */
public class PeerCallActor extends ModuleActor {

    private static final String TAG = "PeerCallActor";

    // Parent Actor for handling events
    private final PeerCallCallback callback;

    // Peer Settings
    private final PeerSettings selfSettings;

    // WebRTC objects
    private List<ApiICEServer> iceServers;
    private HashMap<Long, PeerNodeInt> refs = new HashMap<>();

    private CountedReference<WebRTCMediaStream> currentMediaStream;
    private boolean isCurrentStreamAudioRequired;
    private boolean isCurrentStreamVideoRequired;
    private boolean isAudioRequired;
    private boolean isVideoRequired;

    // State objects
    private boolean isOwnStarted = false;
    private boolean isBuildingStream = false;
    private boolean isAudioEnabled = true;
    private boolean isVideoEnabled = false;

    public PeerCallActor(PeerCallCallback callback, PeerSettings selfSettings, ModuleContext context) {
        super(context);
        this.callback = callback;
        this.selfSettings = selfSettings;
        this.isAudioRequired = true;
        this.isVideoRequired = false;
    }

    //
    // Media Settings
    //

    public void onAudioEnabled(boolean isAudioEnabled) {
        if (this.isAudioEnabled != isAudioEnabled) {
            this.isAudioEnabled = isAudioEnabled;
            if (!isAudioRequired) {
                isAudioRequired = true;
            }
            if (currentMediaStream != null) {
                for (WebRTCMediaTrack audio : currentMediaStream.get().getAudioTracks()) {
                    audio.setEnabled(isAudioEnabled);
                    if (isAudioEnabled) {
                        callback.onOwnTrackAdded(audio);
                    } else {
                        callback.onOwnTrackRemoved(audio);
                    }
                }
            }
            requestStreamIfNeeded();

            for (Long deviceId : refs.keySet()) {
                callback.onMediaStreamsChanged(deviceId, isAudioEnabled, isVideoEnabled);
            }
        }
    }

    public void onVideoEnabled(boolean isVideoEnabled) {
        if (this.isVideoEnabled != isVideoEnabled) {
            this.isVideoEnabled = isVideoEnabled;
            if (!isVideoRequired) {
                isVideoRequired = true;
            }
            if (currentMediaStream != null) {
                for (WebRTCMediaTrack video : currentMediaStream.get().getVideoTracks()) {
                    video.setEnabled(isVideoEnabled);
                    if (isVideoEnabled) {
                        callback.onOwnTrackAdded(video);
                    } else {
                        callback.onOwnTrackRemoved(video);
                    }
                }
            }
            requestStreamIfNeeded();

            for (Long deviceId : refs.keySet()) {
                callback.onMediaStreamsChanged(deviceId, isAudioEnabled, isVideoEnabled);
            }
        }
    }

    public void onOwnStarted() {
        if (isOwnStarted) {
            return;
        }
        isOwnStarted = true;

        requestStreamIfNeeded();
    }

    private void requestStreamIfNeeded() {
        if (!isBuildingStream && isOwnStarted
                && ((currentMediaStream == null)
                || (isCurrentStreamAudioRequired != isAudioRequired)
                || (isCurrentStreamVideoRequired != isVideoRequired))) {

            isBuildingStream = true;
            isCurrentStreamAudioRequired = isAudioRequired;
            isCurrentStreamVideoRequired = isVideoRequired;
            WebRTC.getUserMedia(isAudioRequired, isVideoRequired).then(mediaStream -> {
                isBuildingStream = false;

                // Checking if required types are not changed
                if (isCurrentStreamAudioRequired != isAudioRequired || isCurrentStreamVideoRequired != isVideoRequired) {
                    // Closing unwanted media stream
                    mediaStream.close();
                    requestStreamIfNeeded();
                    return;
                }

                // Update Stream Locally
                CountedReference<WebRTCMediaStream> newStream = new VerboseReference(mediaStream);
                CountedReference<WebRTCMediaStream> oldStream = currentMediaStream;
                currentMediaStream = newStream;

                // Releasing old stream
                if (oldStream != null) {
                    if (isAudioEnabled) {
                        for (WebRTCMediaTrack track : oldStream.get().getAudioTracks()) {
                            callback.onOwnTrackRemoved(track);
                        }
                    }
                    if (isVideoEnabled) {
                        for (WebRTCMediaTrack track : oldStream.get().getVideoTracks()) {
                            callback.onOwnTrackRemoved(track);
                        }
                    }
                    oldStream.release();
                }

                // Connecting to a new stream
                for (WebRTCMediaTrack audio : mediaStream.getAudioTracks()) {
                    audio.setEnabled(isAudioEnabled);
                    if (isAudioEnabled) {
                        callback.onOwnTrackAdded(audio);
                    }
                }
                for (WebRTCMediaTrack video : mediaStream.getVideoTracks()) {
                    video.setEnabled(isVideoEnabled);
                    if (isVideoEnabled) {
                        callback.onOwnTrackAdded(video);
                    }
                }

                // Update references in all active nodes
                for (PeerNodeInt node : refs.values()) {
                    node.replaceOwnStream(newStream);
                }
            }).failure(e -> {
                Log.d(TAG, "Unable to load stream");
                self().send(PoisonPill.INSTANCE);
            });
        }
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
        // Creating Peer Node
        PeerNodeInt peerNodeInt = new PeerNodeInt(deviceId, new NodeCallback(), selfSettings, self(), context());
        // Setting Own Stream if available
        if (currentMediaStream != null) {
            peerNodeInt.replaceOwnStream(currentMediaStream);
        }
        // Set advertise info if available
        if (this.iceServers != null) {
            peerNodeInt.onAdvertisedMaster(iceServers);
        }

        // Notify new node about current streams configuration
        callback.onMediaStreamsChanged(deviceId, isAudioEnabled, isVideoEnabled);

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

        if (currentMediaStream != null) {
            if (isAudioEnabled) {
                for (WebRTCMediaTrack track : currentMediaStream.get().getAudioTracks()) {
                    callback.onOwnTrackRemoved(track);
                }
            }
            if (isVideoEnabled) {
                for (WebRTCMediaTrack track : currentMediaStream.get().getVideoTracks()) {
                    callback.onOwnTrackRemoved(track);
                }
            }
            currentMediaStream.release();
            currentMediaStream = null;
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
            getPeer(candidate.getDeviceId()).onCandidate(candidate.getSessionId(), candidate.getMdpIndex(),
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
        } else if (message instanceof RTCMediaStateUpdated) {
            RTCMediaStateUpdated mediaStateUpdated = (RTCMediaStateUpdated) message;
            getPeer(mediaStateUpdated.getDeviceId()).onMediaStateChanged(mediaStateUpdated.isAudioEnabled(),
                    mediaStateUpdated.isVideoEnabled());
        } else if (message instanceof RTCMasterAdvertised) {
            RTCMasterAdvertised masterAdvertised = (RTCMasterAdvertised) message;
            onMasterAdvertised(masterAdvertised.getIceServers());
        } else if (message instanceof AudioEnabled) {
            AudioEnabled muteChanged = (AudioEnabled) message;
            onAudioEnabled(muteChanged.isEnabled());
        } else if (message instanceof VideoEnabled) {
            VideoEnabled videoEnabled = (VideoEnabled) message;
            onVideoEnabled(videoEnabled.isEnabled());
        } else if (message instanceof OwnStarted) {
            onOwnStarted();
        } else {
            super.onReceive(message);
        }
    }

    public static class AudioEnabled {
        private boolean isEnabled;

        public AudioEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        public boolean isEnabled() {
            return isEnabled;
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
        public void onNegotiationNeeded(long deviceId, long sessionId) {
            callback.onNegotiationNeeded(deviceId, sessionId);
        }

        @Override
        public void onCandidate(long deviceId, long sessionId, int mdpIndex, String id, String sdp) {
            callback.onCandidate(deviceId, sessionId, mdpIndex, id, sdp);
        }

        @Override
        public void onPeerStateChanged(long deviceId, PeerState state) {
            callback.onPeerStateChanged(deviceId, state);
        }

        @Override
        public void onTrackAdded(long deviceId, WebRTCMediaTrack track) {
            callback.onTrackAdded(deviceId, track);
        }

        @Override
        public void onTrackRemoved(long deviceId, WebRTCMediaTrack track) {
            callback.onTrackRemoved(deviceId, track);
        }
    }

    private static int referenceId = 0;

    private static class VerboseReference extends CountedReference<WebRTCMediaStream> {

        private int id = referenceId++;

        public VerboseReference(WebRTCMediaStream value) {
            super(value);
        }

        @Override
        public synchronized void acquire(int counter) {
            Log.d("CountedReference(" + id + ")", "acquire(" + counter + ")");
        }

        @Override
        public synchronized void release(int counter) {
            Log.d("CountedReference(" + id + ")", "release(" + counter + ")");
        }
    }
}