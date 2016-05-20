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
import im.actor.core.modules.calls.peers.messages.RTCNeedOffer;
import im.actor.core.modules.calls.peers.messages.RTCOffer;
import im.actor.core.modules.calls.peers.messages.RTCStart;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.webrtc.WebRTCMediaStream;

/**
 * Proxy Actor for simplifying state of PeerConnection by careful peer connection initialization
 * and handling case when we want to establish connection before call answering
 */
public class PeerNodeActor extends ModuleActor implements PeerConnectionCallback {

    private final long deviceId;
    private final PeerNodeCallback callback;
    private final PeerSettings ownSettings;
    private final ArrayList<WebRTCMediaStream> theirMediaStreams = new ArrayList<>();

    private final HashSet<Long> closedSessions = new HashSet<>();
    private final ArrayList<PendingSession> pendingSessions = new ArrayList<>();

    private long currentSession = 0;
    private PeerState state = PeerState.PENDING;
    private PeerConnectionInt peerConnection;
    private PeerSettings theirSettings;
    private List<ApiICEServer> iceServers;
    private WebRTCMediaStream ownMediaStream;
    private boolean isEnabled = false;
    private boolean isConnected = false;
    private boolean isStarted = false;

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

            for (WebRTCMediaStream mediaStream : theirMediaStreams) {
                mediaStream.setEnabled(true);
            }
        }
    }

    public void addOwnStream(WebRTCMediaStream mediaStream) {
        if (this.ownMediaStream == null) {
            this.ownMediaStream = mediaStream;
            reconfigurePeerConnectionIfNeeded();
        }
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
                    ownMediaStream, this, context(), self(), "connection");

            unstashAll();
        }
    }

    private void startIfNeeded() {
        if (isEnabled && isConnected && !isStarted) {
            isStarted = true;

            state = PeerState.ACTIVE;
            callback.onPeerStateChanged(deviceId, state);

            for (WebRTCMediaStream mediaStream : theirMediaStreams) {
                callback.onStreamAdded(deviceId, mediaStream);
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
    public void onCandidate(int mdpIndex, String id, String sdp) {
        callback.onCandidate(deviceId, mdpIndex, id, sdp);
    }

    @Override
    public void onNegotiationSuccessful(long sessionId) {
        callback.onNegotiationSuccessful(deviceId, sessionId);
    }

    @Override
    public void onStreamAdded(WebRTCMediaStream stream) {
        theirMediaStreams.add(stream);
        stream.setEnabled(isEnabled);
        if (isStarted) {
            callback.onStreamAdded(deviceId, stream);
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
        theirMediaStreams.remove(stream);
        if (isStarted) {
            callback.onStreamRemoved(deviceId, stream);
        }
    }


    public void onCloseSession(long sessionId) {
        if (!closedSessions.contains(sessionId)) {
            closedSessions.add(sessionId);

            currentSession = 0;

            for (PendingSession p : pendingSessions) {
                if (p.getSessionId() == sessionId) {
                    pendingSessions.remove(p);
                    break;
                }
            }
            peerConnection.onResetState();

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
        } else if (message instanceof AddOwnStream) {
            AddOwnStream ownStream = (AddOwnStream) message;
            addOwnStream(ownStream.getMediaStream());
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
                peerConnection.onCandidate(candidate.getMdpIndex(), candidate.getId(), candidate.getSdp());
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
        } else {
            super.onReceive(message);
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

    public static class AddOwnStream {

        private WebRTCMediaStream mediaStream;

        public AddOwnStream(WebRTCMediaStream mediaStream) {
            this.mediaStream = mediaStream;
        }

        public WebRTCMediaStream getMediaStream() {
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