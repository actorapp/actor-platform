package im.actor.core.modules.calls.peers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiICEServer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.messages.RTCAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCAnswer;
import im.actor.core.modules.calls.peers.messages.RTCCandidate;
import im.actor.core.modules.calls.peers.messages.RTCCloseSession;
import im.actor.core.modules.calls.peers.messages.RTCMasterAdvertised;
import im.actor.core.modules.calls.peers.messages.RTCMediaStateUpdated;
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.CountedReference;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;

/**
 * Proxy Actor for simplifying state of PeerConnection by careful peer connection initialization
 * and handling case when we want to establish connection before call answering
 */
public class PeerNodeActor extends ModuleActor implements PeerConnectionCallback {

    //
    // Node Configuration
    //

    /**
     * Current Node's DeviceId
     */
    private final long deviceId;
    /**
     * Callback for a Node events
     */
    private final PeerNodeCallback callback;


    //
    // Connection Configuration
    //
    private final PeerSettings ownSettings;
    private PeerSettings theirSettings;
    private List<ApiICEServer> iceServers;
    private CountedReference<WebRTCMediaStream> ownMediaStream;


    //
    // Signaling State
    //

    /**
     * Current Session Id
     */
    private long currentSession = 0;
    /**
     * All closed sessions. Used to filter out old signaling messages
     */
    private final HashSet<Long> closedSessions = new HashSet<>();
    /**
     * All pending sessions
     */
    private final ArrayList<PendingSession> pendingSessions = new ArrayList<>();


    //
    // Current peer connection
    //
    private int CHILD_NEXT_ID = 0;
    private PeerConnectionInt peerConnection;
    private WebRTCMediaStream theirStream;

    //
    // Node State Values
    //

    /**
     * Mean that if it can produce media tracks
     */
    private boolean isEnabled = false;
    /**
     * State if node is connected to other peer
     */
    private boolean isConnected = false;
    /**
     * State if node is connected, enabled and notified about all streams
     */
    private boolean isStarted = false;
    /**
     * External node state value
     */
    private PeerState state = PeerState.PENDING;
    /**
     * Is Node's audio enabled
     */
    private boolean isAudioEnabled = true;
    /**
     * Is Node's video enabled
     */
    private boolean isVideoEnabled = true;


    public PeerNodeActor(long deviceId,
                         PeerSettings ownSettings,
                         PeerNodeCallback callback,
                         ModuleContext context) {
        super(context);
        this.deviceId = deviceId;
        this.ownSettings = ownSettings;
        this.callback = callback;
    }


    //
    // Starting up peer nodes
    //
    // Stages:
    // 0. Notify about new peer node
    // 1. Waiting for master advertise
    // 2. Waiting for advertise of a node
    // 3. If both peers supports pre connection, create new connection
    // 4. Enabling Own and Their peers
    // 5. Setting own media stream
    // 6. Creation of peer connection
    //

    @Override
    public void preStart() {
        callback.onPeerStateChanged(deviceId, state);
    }

    public void onMasterAdvertised(List<ApiICEServer> iceServers) {
        if (this.iceServers == null) {
            this.iceServers = iceServers;
            reconfigurePeerConnectionIfNeeded();
        }
    }

    public void onAdvertised(PeerSettings settings) {
        if (this.theirSettings == null) {
            this.theirSettings = settings;
            reconfigurePeerConnectionIfNeeded();
        }
    }

    public void onEnabled() {
        if (!isEnabled) {
            isEnabled = true;

            reconfigurePeerConnectionIfNeeded();
        }
    }

    public Promise<Void> replaceOwnStream(CountedReference<WebRTCMediaStream> mediaStream) {
        if (this.ownMediaStream == null) {
            this.ownMediaStream = mediaStream;
            reconfigurePeerConnectionIfNeeded();
        } else {
            this.ownMediaStream.release();
            this.ownMediaStream = mediaStream;
            if (peerConnection != null) {
                return peerConnection.replaceStream(mediaStream);
            }
        }

        return Promise.success(null);
    }

    private void reconfigurePeerConnectionIfNeeded() {
        makePeerConnectionIfNeeded();
        startIfNeeded();
    }

    private void makePeerConnectionIfNeeded() {
        if (peerConnection != null || theirSettings == null || ownMediaStream == null || this.iceServers == null) {
            return;
        }

        if (isEnabled || (theirSettings.isPreConnectionEnabled() && ownSettings.isPreConnectionEnabled())) {
            state = PeerState.CONNECTING;
            callback.onPeerStateChanged(deviceId, state);
            peerConnection = new PeerConnectionInt(
                    iceServers, ownSettings, theirSettings,
                    ownMediaStream, this, context(), self(), "connection/" + (CHILD_NEXT_ID++));
            unstashAll();
        }
    }

    private void startIfNeeded() {
        if (isEnabled && isConnected && !isStarted) {
            isStarted = true;

            state = PeerState.ACTIVE;
            callback.onPeerStateChanged(deviceId, state);

            if (theirStream != null) {
                for (WebRTCMediaTrack track : theirStream.getAudioTracks()) {
                    track.setEnabled(isAudioEnabled);
                    if (isAudioEnabled) {
                        callback.onTrackAdded(deviceId, track);
                    }
                }
                for (WebRTCMediaTrack track : theirStream.getVideoTracks()) {
                    track.setEnabled(isVideoEnabled);
                    if (isVideoEnabled) {
                        callback.onTrackAdded(deviceId, track);
                    }
                }
            }
        }
    }


    //
    // Peer callbacks
    //

    @Override
    public void onOffer(long sessionId, String sdp) {
        callback.onOffer(deviceId, sessionId, sdp);
    }

    @Override
    public void onAnswer(long sessionId, String sdp) {
        callback.onAnswer(deviceId, sessionId, sdp);
    }

    @Override
    public void onCandidate(long sessionId, int mdpIndex, String id, String sdp) {
        callback.onCandidate(deviceId, sessionId, mdpIndex, id, sdp);
    }

    @Override
    public void onNegotiationSuccessful(long sessionId) {
        callback.onNegotiationSuccessful(deviceId, sessionId);
    }

    @Override
    public void onNegotiationNeeded(long sessionId) {
        callback.onNegotiationNeeded(deviceId, sessionId);
    }

    @Override
    public void onStreamAdded(WebRTCMediaStream stream) {
        WebRTCMediaStream oldStream = theirStream;
        theirStream = stream;

        //
        // Enable Tracks if needed
        //
        if (isStarted) {
            for (WebRTCMediaTrack track : stream.getAudioTracks()) {
                track.setEnabled(isAudioEnabled);
                if (isAudioEnabled) {
                    callback.onTrackAdded(deviceId, track);
                }
            }
            for (WebRTCMediaTrack track : stream.getVideoTracks()) {
                track.setEnabled(isVideoEnabled);
                if (isVideoEnabled) {
                    callback.onTrackAdded(deviceId, track);
                }
            }

            if (oldStream != null) {
                for (WebRTCMediaTrack track : oldStream.getVideoTracks()) {
                    callback.onTrackRemoved(deviceId, track);
                }
                for (WebRTCMediaTrack track : oldStream.getAudioTracks()) {
                    callback.onTrackRemoved(deviceId, track);
                }
            }
        }

        if (!isConnected) {
            isConnected = true;
            if (!isEnabled) {
                state = PeerState.CONNECTED;
                callback.onPeerStateChanged(deviceId, state);
            } else {
                // This case is handled in startIfNeeded();
            }
        }

        startIfNeeded();
    }

    @Override

    public void onStreamRemoved(WebRTCMediaStream stream) {
        if (isAudioEnabled || isVideoEnabled) {
            return;
        }
        //
        // Remove Tracks if needed
        //
        if (isStarted && theirStream != null) {
            for (WebRTCMediaTrack track : stream.getAudioTracks()) {
                callback.onTrackRemoved(deviceId, track);
            }
            for (WebRTCMediaTrack track : stream.getVideoTracks()) {
                callback.onTrackRemoved(deviceId, track);
            }
        }
    }

    public void onStreamStateChanged(boolean isAudioEnabled, boolean isVideoEnabled) {
        if (this.isAudioEnabled != isAudioEnabled) {
            this.isAudioEnabled = isAudioEnabled;
            if (isStarted) {
                for (WebRTCMediaTrack track : theirStream.getAudioTracks()) {
                    track.setEnabled(isAudioEnabled);
                    if (isAudioEnabled) {
                        callback.onTrackAdded(deviceId, track);
                    } else {
                        callback.onTrackRemoved(deviceId, track);
                    }
                }
            }
        }
        if (this.isVideoEnabled != isVideoEnabled) {
            this.isVideoEnabled = isVideoEnabled;
            if (isStarted) {
                for (WebRTCMediaTrack track : theirStream.getVideoTracks()) {
                    track.setEnabled(isVideoEnabled);
                    if (isVideoEnabled) {
                        callback.onTrackAdded(deviceId, track);
                    } else {
                        callback.onTrackRemoved(deviceId, track);
                    }
                }
            }
        }
    }

    public void onCloseSession(long sessionId) {
        if (!closedSessions.contains(sessionId)) {
            closedSessions.add(sessionId);

            currentSession = 0;

            // Searching for pending sessions and closing it
            for (PendingSession p : pendingSessions) {
                if (p.getSessionId() == sessionId) {
                    pendingSessions.remove(p);
                    break;
                }
            }

            // Killing Peer Connection
            if (peerConnection != null) {
                peerConnection.kill();
                peerConnection = null;
            }

            // Creating new peer connection
            peerConnection = new PeerConnectionInt(
                    iceServers, ownSettings, theirSettings,
                    ownMediaStream, this, context(), self(), "connection/" + (CHILD_NEXT_ID++));

            // Pick first pending session if available
            if (pendingSessions.size() > 0) {
                PendingSession p = pendingSessions.remove(0);
                if (p != null) {
                    for (Object o : p.getMessages()) {
                        self().sendFirst(o, self());
                    }
                }
            }
        }
    }

    //
    // Stopping
    //

    @Override
    public void postStop() {
        if (peerConnection != null) {
            peerConnection.kill();
            peerConnection = null;
        }
        if (ownMediaStream != null) {
            ownMediaStream.release();
            ownMediaStream = null;
        }
        state = PeerState.DISPOSED;
        callback.onPeerStateChanged(deviceId, state);
    }


    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof RTCStart) {
            onEnabled();
        } else if (message instanceof RTCAdvertised) {
            RTCAdvertised advertised = (RTCAdvertised) message;
            onAdvertised(advertised.getSettings());
        } else if (message instanceof ReplaceOwnStream) {
            ReplaceOwnStream ownStream = (ReplaceOwnStream) message;
            replaceOwnStream(ownStream.getMediaStream());
        } else if (message instanceof RTCMasterAdvertised) {
            RTCMasterAdvertised advertisedMaster = (RTCMasterAdvertised) message;
            onMasterAdvertised(advertisedMaster.getIceServers());
        } else if (message instanceof RTCNeedOffer) {
            RTCNeedOffer needOffer = (RTCNeedOffer) message;
            if (peerConnection != null) {
                if (receiveSessionMessage(needOffer.getSessionId(), message)) {
                    peerConnection.onOfferNeeded(needOffer.getSessionId());
                }
            } else {
                stash();
            }
        } else if (message instanceof RTCOffer) {
            RTCOffer offer = (RTCOffer) message;
            if (peerConnection != null) {
                if (receiveSessionMessage(offer.getSessionId(), message)) {
                    peerConnection.onOffer(offer.getSessionId(), offer.getSdp());
                }
            } else {
                stash();
            }
        } else if (message instanceof RTCAnswer) {
            RTCAnswer answer = (RTCAnswer) message;
            if (peerConnection != null) {
                if (receiveSessionMessage(answer.getSessionId(), message)) {
                    peerConnection.onAnswer(answer.getSessionId(), answer.getSdp());
                }
            } else {
                stash();
            }
        } else if (message instanceof RTCCandidate) {
            RTCCandidate candidate = (RTCCandidate) message;
            if (peerConnection != null) {
                peerConnection.onCandidate(candidate.getSessionId(), candidate.getMdpIndex(), candidate.getId(), candidate.getSdp());
            } else {
                stash();
            }
        } else if (message instanceof RTCCloseSession) {
            RTCCloseSession closeSession = (RTCCloseSession) message;
            if (peerConnection != null) {
                onCloseSession(closeSession.getSessionId());
            } else {
                stash();
            }
        } else if (message instanceof RTCMediaStateUpdated) {
            RTCMediaStateUpdated stateUpdated = (RTCMediaStateUpdated) message;
            onStreamStateChanged(stateUpdated.isAudioEnabled(), stateUpdated.isVideoEnabled());
        } else {
            super.onReceive(message);
        }
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof ReplaceOwnStream) {
            ReplaceOwnStream ownStream = (ReplaceOwnStream) message;
            return replaceOwnStream(ownStream.getMediaStream());
        } else {
            return super.onAsk(message);
        }
    }

    private boolean receiveSessionMessage(long sessionId, Object msg) {
        if (currentSession == sessionId || currentSession == 0) {
            currentSession = sessionId;
            return true;
        } else {
            if (!closedSessions.contains(sessionId)) {
                boolean found = false;
                for (PendingSession p : pendingSessions) {
                    if (p.getSessionId() == sessionId) {
                        p.getMessages().add(msg);
                        found = true;
                    }
                }
                if (!found) {
                    PendingSession p = new PendingSession(sessionId);
                    p.getMessages().add(msg);
                    pendingSessions.add(p);
                }
            }
            return false;
        }
    }

    public static class ReplaceOwnStream implements AskMessage<Void> {

        private CountedReference<WebRTCMediaStream> mediaStream;

        public ReplaceOwnStream(CountedReference<WebRTCMediaStream> mediaStream) {
            this.mediaStream = mediaStream;
        }

        public CountedReference<WebRTCMediaStream> getMediaStream() {
            return mediaStream;
        }
    }

    private class PendingSession {

        private long sessionId;
        private ArrayList<Object> messages;

        public PendingSession(long sessionId) {
            this.sessionId = sessionId;
            this.messages = new ArrayList<>();
        }

        public long getSessionId() {
            return sessionId;
        }

        public ArrayList<Object> getMessages() {
            return messages;
        }
    }
}