package im.actor.core.modules.calls;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.RequestJoinCall;
import im.actor.core.api.rpc.RequestRejectCall;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.AbsCallActor;
import im.actor.core.modules.calls.peers.CallBusActor;
import im.actor.core.viewmodel.CallMediaSource;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.Log;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.power.WakeLock;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

import static im.actor.core.entity.EntityConverter.convert;

public class CallActor extends AbsCallActor {

    private final boolean isMaster;
    private final WakeLock wakeLock;
    private long callId;
    private Peer peer;

    private HashMap<Long, PeerConnectionHolder> mediaSources = new HashMap<>();

    private CallVM callVM;
    private CommandCallback<Long> callback;

    private boolean isActive;
    private boolean isAnswered;
    private boolean isRejected;

    private boolean isVideoInitiallyEnabled;

    public CallActor(long callId, WakeLock wakeLock, ModuleContext context) {
        super(context);
        this.wakeLock = wakeLock;
        this.isMaster = false;
        this.callId = callId;
        this.isAnswered = false;
        this.isActive = false;
    }

    public CallActor(Peer peer, CommandCallback<Long> callback, WakeLock wakeLock, boolean isVideoInitiallyEnabled, ModuleContext context) {
        super(context);
        this.wakeLock = wakeLock;
        this.isMaster = true;
        this.callback = callback;
        this.peer = peer;
        this.isAnswered = true;
        this.isActive = false;
        this.isVideoInitiallyEnabled = isVideoInitiallyEnabled;
    }

    @Override
    public void preStart() {
        super.preStart();
        if (isMaster) {
            api(new RequestDoCall(buidOutPeer(peer), CallBusActor.TIMEOUT)).then(responseDoCall -> {
                callId = responseDoCall.getCallId();
                callBus.joinMasterBus(responseDoCall.getEventBusId(), responseDoCall.getDeviceId());
                callBus.changeVideoEnabled(isVideoInitiallyEnabled);
                callBus.startOwn();
                callVM = callViewModels.spawnNewOutgoingVM(responseDoCall.getCallId(), peer);
                callVM.getIsVideoEnabled().change(isVideoInitiallyEnabled);
            }).failure(e -> self().send(PoisonPill.INSTANCE));
        } else {
            api(new RequestGetCallInfo(callId)).then(responseGetCallInfo -> {
                peer = convert(responseGetCallInfo.getPeer());
                callBus.joinBus(responseGetCallInfo.getEventBusId());
                callVM = callViewModels.spawnNewIncomingVM(callId, peer, CallState.RINGING);
                callVM.getIsVideoEnabled().change(isVideoInitiallyEnabled);
            }).failure(e -> self().send(PoisonPill.INSTANCE));
        }
    }


    //
    // Call lifecycle
    //

    @Override
    public void onBusStarted(@NotNull String busId) {
        if (isMaster) {
            callManager.send(new CallManagerActor.DoCallComplete(callId), self());

            callback.onResult(callId);
            callback = null;
        } else {
            callManager.send(new CallManagerActor.IncomingCallReady(callId), self());
        }
    }

    @Override
    public void onCallConnected() {
        // callVM.getState().change()
    }

    @Override
    public void onCallEnabled() {
        isActive = true;
        if (isAnswered) {
            callVM.getState().change(CallState.IN_PROGRESS);
            callVM.setCallStart(im.actor.runtime.Runtime.getCurrentTime());
        }
        if (isMaster) {
            callManager.send(new CallManagerActor.OnCallAnswered(callId), self());
        }
    }

    public void onAnswerCall() {
        if (!isAnswered && !isRejected) {
            isAnswered = true;
            callBus.startOwn();
            request(new RequestJoinCall(callId));

            if (isActive) {
                callVM.getState().change(CallState.IN_PROGRESS);
                callVM.setCallStart(im.actor.runtime.Runtime.getCurrentTime());
            } else {
                callVM.getState().change(CallState.CONNECTING);
            }
        }
    }

    public void onRejectCall() {
        if (!isAnswered && !isRejected) {
            isRejected = true;
            request(new RequestRejectCall(callId));
            self().send(PoisonPill.INSTANCE);
        }
    }

    @Override
    public void onBusStopped() {
        self().send(PoisonPill.INSTANCE);
    }


    //
    // Incoming Connections
    //

    @Override
    public void onPeerConnectionStateChanged(long deviceId, boolean isAudioEnabled, boolean isVideoEnabled) {
        PeerConnectionHolder holder = getHolder(deviceId);
        if (holder.isVideoEnabled != isVideoEnabled || holder.isAudioEnabled != isAudioEnabled) {
            holder.setAudioEnabled(isAudioEnabled);
            holder.setVideoEnabled(isVideoEnabled);

            ArrayList<CallMediaSource> mediaSources = new ArrayList<>(callVM.getMediaStreams().get());
            for (int i = 0; i < mediaSources.size(); i++) {
                CallMediaSource mediaSource = mediaSources.get(i);
                if (mediaSource.getDeviceId() == deviceId) {
                    mediaSources.remove(i);
                    mediaSources.add(i, new CallMediaSource(deviceId,
                            isAudioEnabled, isAudioEnabled, mediaSource.getStream()));
                }
            }
            callVM.getMediaStreams().change(mediaSources);
        }
    }

    @Override
    public void onStreamAdded(long deviceId, WebRTCMediaStream stream) {
        PeerConnectionHolder holder = getHolder(deviceId);
        ArrayList<CallMediaSource> mediaSources = new ArrayList<>(callVM.getMediaStreams().get());
        mediaSources.add(new CallMediaSource(deviceId, holder.isAudioEnabled(), holder.isVideoEnabled(), stream));
        callVM.getMediaStreams().change(mediaSources);
    }

    @Override
    public void onStreamRemoved(long deviceId, WebRTCMediaStream stream) {
        ArrayList<CallMediaSource> mediaSources = new ArrayList<>(callVM.getMediaStreams().get());
        for (int i = 0; i < mediaSources.size(); i++) {
            CallMediaSource mediaSource = mediaSources.get(i);
            if (mediaSource.getDeviceId() == deviceId && mediaSource.getStream() == stream) {
                mediaSources.remove(i);
                break;
            }
        }
        callVM.getMediaStreams().change(mediaSources);
    }

    @Override
    public void onPeerConnectionDisposed(long deviceId) {

    }

    private PeerConnectionHolder getHolder(long deviceId) {
        if (!mediaSources.containsKey(deviceId)) {
            mediaSources.put(deviceId, new PeerConnectionHolder());
        }
        return mediaSources.get(deviceId);
    }

    //
    // Outgoing Stream
    //

    @Override
    public void onOwnStreamAdded(WebRTCMediaStream stream) {
        callVM.getOwnMediaStream().change(stream);
    }

    @Override
    public void onOwnStreamRemoved(WebRTCMediaStream stream) {
        callVM.getOwnMediaStream().change(null);
    }

    @Override
    public void onAudioEnableChanged(boolean enabled) {
        super.onAudioEnableChanged(enabled);
        callVM.getIsMuted().change(!enabled);
    }

    @Override
    public void onVideoEnableChanged(boolean enabled) {
        super.onVideoEnableChanged(enabled);
        callVM.getIsVideoEnabled().change(enabled);
    }


    //
    // Cleanup
    //

    @Override
    public void postStop() {
        super.postStop();
        if (callVM != null) {
            callVM.getState().change(CallState.ENDED);
            callVM.setCallEnd(im.actor.runtime.Runtime.getCurrentTime());
        }
        callBus.kill();
        if (callId != 0) {
            callManager.send(new CallManagerActor.OnCallEnded(callId), self());
        }
        wakeLock.releaseLock();
    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof AnswerCall) {
            onAnswerCall();
        } else if (message instanceof RejectCall) {
            onRejectCall();
        } else {
            super.onReceive(message);
        }
    }

    public static class AnswerCall {

    }

    public static class RejectCall {

    }

    private static class PeerConnectionHolder {

        private boolean isAudioEnabled;
        private boolean isVideoEnabled;
        private boolean isEnabled;
        private ArrayList<WebRTCMediaStream> streams;

        public PeerConnectionHolder() {
            this.isEnabled = false;
            this.isAudioEnabled = true;
            this.isVideoEnabled = false;
            this.streams = new ArrayList<>();
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public boolean isAudioEnabled() {
            return isAudioEnabled;
        }

        public void setAudioEnabled(boolean audioEnabled) {
            isAudioEnabled = audioEnabled;
        }

        public ArrayList<WebRTCMediaStream> getStreams() {
            return streams;
        }

        public boolean isVideoEnabled() {
            return isVideoEnabled;
        }

        public void setVideoEnabled(boolean videoEnabled) {
            isVideoEnabled = videoEnabled;
        }
    }
}
