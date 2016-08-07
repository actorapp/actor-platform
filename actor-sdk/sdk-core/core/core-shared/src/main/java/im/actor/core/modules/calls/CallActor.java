package im.actor.core.modules.calls;

import org.jetbrains.annotations.NotNull;

import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.RequestJoinCall;
import im.actor.core.api.rpc.RequestRejectCall;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.AbsCallActor;
import im.actor.core.modules.calls.peers.CallBusActor;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.generics.ArrayListMediaTrack;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.power.WakeLock;
import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCTrackType;

import static im.actor.core.entity.EntityConverter.convert;

public class CallActor extends AbsCallActor {

    private final boolean isMaster;
    private final WakeLock wakeLock;
    private long callId;
    private Peer peer;

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
            api(new RequestDoCall(buidOutPeer(peer), CallBusActor.TIMEOUT, false, false, isVideoInitiallyEnabled)).then(responseDoCall -> {
                callId = responseDoCall.getCallId();
                callBus.joinMasterBus(responseDoCall.getEventBusId(), responseDoCall.getDeviceId());
                callBus.changeVideoEnabled(isVideoInitiallyEnabled);
                callBus.startOwn();
                callVM = callViewModels.spawnNewOutgoingVM(responseDoCall.getCallId(), peer, isVideoInitiallyEnabled,
                        isVideoInitiallyEnabled);
            }).failure(e -> self().send(PoisonPill.INSTANCE));
        } else {
            api(new RequestGetCallInfo(callId)).then(responseGetCallInfo -> {
                peer = convert(responseGetCallInfo.getPeer());
                callBus.joinBus(responseGetCallInfo.getEventBusId());
                if (responseGetCallInfo.isVideoPreferred() != null) {
                    isVideoInitiallyEnabled = responseGetCallInfo.isVideoPreferred();
                    callBus.changeVideoEnabled(isVideoInitiallyEnabled);
                }
                callVM = callViewModels.spawnNewIncomingVM(callId, peer, isVideoInitiallyEnabled,
                        isVideoInitiallyEnabled, CallState.RINGING);
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
    // Track Events
    //
    @Override
    public void onTrackAdded(long deviceId, WebRTCMediaTrack track) {
        if (track.getTrackType() == WebRTCTrackType.AUDIO) {
            ArrayListMediaTrack tracks = new ArrayListMediaTrack(callVM.getTheirAudioTracks().get());
            tracks.add(track);
            callVM.getTheirAudioTracks().change(tracks);
        } else if (track.getTrackType() == WebRTCTrackType.VIDEO) {
            ArrayListMediaTrack tracks = new ArrayListMediaTrack(callVM.getTheirVideoTracks().get());
            tracks.add(track);
            callVM.getTheirVideoTracks().change(tracks);
        } else {
            // Unknown track type
        }
    }

    @Override
    public void onTrackRemoved(long deviceId, WebRTCMediaTrack track) {
        if (track.getTrackType() == WebRTCTrackType.AUDIO) {
            ArrayListMediaTrack tracks = new ArrayListMediaTrack(callVM.getTheirAudioTracks().get());
            tracks.remove(track);
            callVM.getTheirAudioTracks().change(tracks);
        } else if (track.getTrackType() == WebRTCTrackType.VIDEO) {
            ArrayListMediaTrack tracks = new ArrayListMediaTrack(callVM.getTheirVideoTracks().get());
            tracks.remove(track);
            callVM.getTheirVideoTracks().change(tracks);
        } else {
            // Unknown track type
        }
    }

    @Override
    public void onOwnTrackAdded(WebRTCMediaTrack track) {
        if (track.getTrackType() == WebRTCTrackType.AUDIO) {
            ArrayListMediaTrack tracks = new ArrayListMediaTrack(callVM.getOwnAudioTracks().get());
            tracks.add(track);
            callVM.getOwnAudioTracks().change(tracks);
        } else if (track.getTrackType() == WebRTCTrackType.VIDEO) {
            ArrayListMediaTrack tracks = new ArrayListMediaTrack(callVM.getOwnVideoTracks().get());
            tracks.add(track);
            callVM.getOwnVideoTracks().change(tracks);
        } else {
            // Unknown track type
        }
    }

    @Override
    public void onOwnTrackRemoved(WebRTCMediaTrack track) {
        if (track.getTrackType() == WebRTCTrackType.AUDIO) {
            ArrayListMediaTrack tracks = new ArrayListMediaTrack(callVM.getOwnAudioTracks().get());
            tracks.remove(track);
            callVM.getOwnAudioTracks().change(tracks);
        } else if (track.getTrackType() == WebRTCTrackType.VIDEO) {
            ArrayListMediaTrack tracks = new ArrayListMediaTrack(callVM.getOwnVideoTracks().get());
            tracks.remove(track);
            callVM.getOwnVideoTracks().change(tracks);
        } else {
            // Unknown track type
        }
    }

    @Override
    public void onAudioEnableChanged(boolean enabled) {
        super.onAudioEnableChanged(enabled);
        callVM.getIsAudioEnabled().change(enabled);
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
}
