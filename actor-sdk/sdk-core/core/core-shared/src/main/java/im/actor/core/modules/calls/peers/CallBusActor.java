package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.api.ApiAdvertiseMaster;
import im.actor.core.api.ApiAdvertiseSelf;
import im.actor.core.api.ApiAnswer;
import im.actor.core.api.ApiCandidate;
import im.actor.core.api.ApiCloseSession;
import im.actor.core.api.ApiEnableConnection;
import im.actor.core.api.ApiMediaStreamsUpdated;
import im.actor.core.api.ApiNeedDisconnect;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiNegotinationSuccessful;
import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiOnRenegotiationNeeded;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;

/*-[
#pragma clang diagnostic ignored "-Wnullability-completeness"
]-*/

public class CallBusActor extends EventBusActor implements PeerCallCallback {

    private static final int STASH = 1;

    public static final long TIMEOUT = 18000;

    @NotNull
    private final PeerSettings selfSettings;
    @NotNull
    private final PeerCallCallback peerCallback;
    @NotNull
    private final CallBusCallback callBusCallback;

    private boolean isMasterReady;
    private long masterDeviceId;
    @Nullable
    private PeerCallInt peerCall;
    private boolean isConnected = false;
    private boolean isEnabled = false;

    public CallBusActor(@NotNull final CallBusCallback callBusCallback,
                        @NotNull PeerSettings selfSettings,
                        @NotNull ModuleContext context) {
        super(context);
        this.selfSettings = selfSettings;
        this.callBusCallback = callBusCallback;
        this.peerCallback = new CallbackWrapper(this);
    }


    @Override
    public void preStart() {
        super.preStart();

        ActorRef ref = system().actorOf(getPath() + "/peer", () -> {
            return new PeerCallActor(peerCallback, CallBusActor.this.selfSettings, context());
        });
        this.peerCall = new PeerCallInt(ref);
    }

    @Override
    public void onBusStarted() {
        super.onBusStarted();
        callBusCallback.onBusStarted(getBusId());
    }


    //
    // PeerCall callback
    //

    @Override
    public void onOffer(long deviceId, long sessionId, @NotNull String sdp) {
        sendSignal(deviceId, new ApiOffer(sessionId, sdp, CallBusActor.this.selfSettings.toApi()));
    }

    @Override
    public void onAnswer(long deviceId, long sessionId, @NotNull String sdp) {
        sendSignal(deviceId, new ApiAnswer(sessionId, sdp));
    }

    @Override
    public void onCandidate(long deviceId, long sessionId, int mdpIndex, @NotNull String id, @NotNull String sdp) {
        sendSignal(deviceId, new ApiCandidate(sessionId, mdpIndex, id, sdp));
    }

    @Override
    public void onNegotiationSuccessful(final long deviceId, final long sessionId) {
        if (isMasterReady) {
            sendSignal(masterDeviceId, new ApiNegotinationSuccessful(deviceId, sessionId));
        } else {
            stash(STASH);
        }
    }

    @Override
    public void onNegotiationNeeded(long deviceId, long sessionId) {
        if (isMasterReady) {
            sendSignal(masterDeviceId, new ApiOnRenegotiationNeeded(deviceId, sessionId));
        } else {
            stash(STASH);
        }
    }

    @Override
    public void onMediaStreamsChanged(long deviceId, boolean isAudioEnabled, boolean isVideoEnabled) {
        sendSignal(deviceId, new ApiMediaStreamsUpdated(isAudioEnabled, isVideoEnabled));
    }

    @Override
    public void onPeerStateChanged(long deviceId, @NotNull PeerState state) {
        if (state == PeerState.CONNECTED && !isConnected && !isEnabled) {
            isConnected = true;
            callBusCallback.onCallConnected();
        }
        if (state == PeerState.ACTIVE && !isEnabled) {
            isEnabled = true;
            callBusCallback.onCallEnabled();
        }
    }

    @Override
    public void onTrackAdded(long deviceId, WebRTCMediaTrack track) {
        callBusCallback.onTrackAdded(deviceId, track);
    }

    @Override
    public void onTrackRemoved(long deviceId, WebRTCMediaTrack track) {
        callBusCallback.onTrackRemoved(deviceId, track);
    }

    @Override
    public void onOwnTrackAdded(WebRTCMediaTrack track) {
        callBusCallback.onOwnTrackAdded(track);
    }

    @Override
    public void onOwnTrackRemoved(WebRTCMediaTrack track) {
        callBusCallback.onOwnTrackRemoved(track);
    }

    //
    // Actions
    //

    public void onChangeAudioEnabled(boolean isEnabled) {
        peerCall.onAudioEnabledChanged(isEnabled);
    }

    public void onChangeVideoEnabled(boolean isEnabled) {
        peerCall.onVideoEnabledChanged(isEnabled);
    }

    public void onOwnAnswered() {
        peerCall.onOwnStarted();
    }

    //
    // Event Bus handler
    //

    @Override
    public void onDeviceConnected(int uid, long deviceId) {

    }

    @Override
    public void onDeviceDisconnected(int uid, long deviceId) {
        peerCall.disposePeer(deviceId);
    }

    @Override
    public final void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
        if (senderId == null || senderDeviceId == null) {
            return;
        }

        ApiWebRTCSignaling signal;
        try {
            signal = ApiWebRTCSignaling.fromBytes(data);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Log.d("CallBusActor", "Message Received: " + signal);

        if (signal instanceof ApiAnswer) {
            ApiAnswer answer = (ApiAnswer) signal;
            peerCall.onAnswer(senderDeviceId, answer.getSessionId(), answer.getSdp());
        } else if (signal instanceof ApiOffer) {
            ApiOffer offer = (ApiOffer) signal;
            peerCall.onAdvertised(senderDeviceId, new PeerSettings(offer.getOwnPeerSettings()));
            peerCall.onOffer(senderDeviceId, offer.getSessionId(), offer.getSdp());
        } else if (signal instanceof ApiCandidate) {
            ApiCandidate candidate = (ApiCandidate) signal;
            peerCall.onCandidate(senderDeviceId, candidate.getSessionId(), candidate.getIndex(), candidate.getId(), candidate.getSdp());
        } else if (signal instanceof ApiNeedOffer) {
            ApiNeedOffer needOffer = (ApiNeedOffer) signal;
            peerCall.onAdvertised(needOffer.getDevice(), new PeerSettings(needOffer.getPeerSettings()));
            peerCall.onOfferNeeded(needOffer.getDevice(), needOffer.getSessionId());
        } else if (signal instanceof ApiNeedDisconnect) {
            ApiNeedDisconnect disconnect = (ApiNeedDisconnect) signal;
            peerCall.disposePeer(disconnect.getDevice());
        } else if (signal instanceof ApiEnableConnection) {
            ApiEnableConnection connection = (ApiEnableConnection) signal;
            peerCall.onOwnStarted();
            peerCall.onTheirStarted(connection.getDevice());
        } else if (signal instanceof ApiCloseSession) {
            ApiCloseSession closeSession = (ApiCloseSession) signal;
            peerCall.closeSession(closeSession.getDevice(), closeSession.getSessionId());
        } else if (signal instanceof ApiAdvertiseMaster) {
            ApiAdvertiseMaster advertiseMaster = (ApiAdvertiseMaster) signal;
            if (isMasterReady) {
                return;
            }
            isMasterReady = true;
            masterDeviceId = senderDeviceId;
            unstashAll(STASH);

            //
            // Advertise own settings to master device
            //
            sendSignal(masterDeviceId, new ApiAdvertiseSelf(selfSettings.toApi()));

            //
            // Sending Configuration to Peer Call
            //
            peerCall.onConfigurationReady(advertiseMaster.getServer());
        } else if (signal instanceof ApiMediaStreamsUpdated) {
            ApiMediaStreamsUpdated streamsUpdated = (ApiMediaStreamsUpdated) signal;
            Boolean isAudioEnabled = streamsUpdated.isAudioEnabled();
            if (isAudioEnabled == null) {
                isAudioEnabled = true;
            }
            Boolean isVideoEnabled = streamsUpdated.isVideoEnabled();
            if (isVideoEnabled == null) {
                isVideoEnabled = true;
            }

            // Notify About Media State Changes
            peerCall.onMediaStateChanged(senderDeviceId, isAudioEnabled, isVideoEnabled);
        }
    }

    public final void sendSignal(long deviceId, @NotNull ApiWebRTCSignaling signal) {
        Log.d("CallBusActor", "Message Sent: " + signal);
        try {
            sendMessage(deviceId, signal.buildContainer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postStop() {
        super.postStop();
        if (peerCall != null) {
            peerCall.kill();
            peerCall = null;
        }
        callBusCallback.onBusStopped();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof JoinBus) {
            joinBus(((JoinBus) message).getBusId(), TIMEOUT);
        } else if (message instanceof JoinMasterBus) {
            JoinMasterBus joinMasterBus = (JoinMasterBus) message;
            connectBus(joinMasterBus.getBusId(), joinMasterBus.getDeviceId(), TIMEOUT, true);
        } else if (message instanceof AudioEnabled) {
            onChangeAudioEnabled(((AudioEnabled) message).isEnabled());
        } else if (message instanceof VideoEnabled) {
            onChangeVideoEnabled(((VideoEnabled) message).isEnabled());
        } else if (message instanceof OnAnswered) {
            onOwnAnswered();
        } else {
            super.onReceive(message);
        }
    }

    public static class JoinBus {

        @NotNull
        private String busId;

        public JoinBus(@NotNull String busId) {
            this.busId = busId;
        }

        @NotNull
        public String getBusId() {
            return busId;
        }
    }

    public static class JoinMasterBus {

        @NotNull
        private String busId;
        private long deviceId;

        public JoinMasterBus(@NotNull String busId, long deviceId) {
            this.busId = busId;
            this.deviceId = deviceId;
        }

        @NotNull
        public String getBusId() {
            return busId;
        }

        public long getDeviceId() {
            return deviceId;
        }
    }

    public static class AudioEnabled {
        private boolean enabled;

        public AudioEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
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

    public static class OnAnswered {

    }

    public class CallbackWrapper implements PeerCallCallback {

        @NotNull
        private final PeerCallCallback callCallback;

        public CallbackWrapper(@NotNull PeerCallCallback callCallback) {
            this.callCallback = callCallback;
        }

        @Override
        public void onOffer(final long deviceId, final long sessionId, @NotNull final String sdp) {
            self().post(() -> callCallback.onOffer(deviceId, sessionId, sdp));
        }

        @Override
        public void onAnswer(final long deviceId, final long sessionId, @NotNull final String sdp) {
            self().post(() -> callCallback.onAnswer(deviceId, sessionId, sdp));
        }

        @Override
        public void onCandidate(final long deviceId, final long sessionId, final int mdpIndex, @NotNull final String id, @NotNull final String sdp) {
            self().post(() -> callCallback.onCandidate(deviceId, sessionId, mdpIndex, id, sdp));
        }

        @Override
        public void onNegotiationSuccessful(final long deviceId, final long sessionId) {
            self().post(() -> callCallback.onNegotiationSuccessful(deviceId, sessionId));
        }

        @Override
        public void onNegotiationNeeded(long deviceId, long sessionId) {
            self().post(() -> callCallback.onNegotiationNeeded(deviceId, sessionId));
        }

        @Override
        public void onMediaStreamsChanged(long deviceId, boolean isAudioEnabled, boolean isVideoEnabled) {
            self().post(() -> callCallback.onMediaStreamsChanged(deviceId, isAudioEnabled, isVideoEnabled));
        }

        @Override
        public void onPeerStateChanged(final long deviceId, @NotNull final PeerState state) {
            self().post(() -> callCallback.onPeerStateChanged(deviceId, state));
        }

        @Override
        public void onTrackAdded(long deviceId, WebRTCMediaTrack track) {
            self().post(() -> callCallback.onTrackAdded(deviceId, track));
        }

        @Override
        public void onTrackRemoved(long deviceId, WebRTCMediaTrack track) {
            self().post(() -> callCallback.onTrackRemoved(deviceId, track));
        }

        @Override
        public void onOwnTrackAdded(WebRTCMediaTrack track) {
            self().post(() -> callCallback.onOwnTrackAdded(track));
        }

        @Override
        public void onOwnTrackRemoved(WebRTCMediaTrack track) {
            self().post(() -> callCallback.onOwnTrackRemoved(track));
        }
    }
}
