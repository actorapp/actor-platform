package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.NotNull;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.CallViewModels;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.WebRTC;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public abstract class AbsCallActor extends ModuleActor implements CallBusCallback {

    protected final PeerSettings selfSettings;
    protected final CallViewModels callViewModels;
    protected final ActorRef callManager;
    protected CallBusInt callBus;

    public AbsCallActor(ModuleContext context) {
        super(context);

        this.callManager = context.getCallsModule().getCallManager();
        this.callViewModels = context().getCallsModule().getCallViewModels();
        this.selfSettings = new PeerSettings();
        this.selfSettings.setIsPreConnectionEnabled(WebRTC.isSupportsPreConnections());
    }

    @Override
    public void preStart() {
        super.preStart();
        callBus = new CallBusInt(system().actorOf(getPath() + "/bus", () -> {
            return new CallBusActor(new CallBusCallbackWrapper(), selfSettings, context());
        }));
    }

    public void onAudioEnableChanged(boolean enabled) {
        callBus.changeAudioEnabled(enabled);
    }


    public void onVideoEnableChanged(boolean enabled) {
        callBus.changeVideoEnabled(enabled);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof AudioEnabled) {
            onAudioEnableChanged(((AudioEnabled) message).isEnabled());
        } else if (message instanceof VideoEnabled) {
            onVideoEnableChanged(((VideoEnabled) message).isEnabled());
        } else {
            super.onReceive(message);
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

    //
    // Wrapper
    //

    private class CallBusCallbackWrapper implements CallBusCallback {

        @Override
        public void onBusStarted(@NotNull final String busId) {
            self().post(() -> AbsCallActor.this.onBusStarted(busId));
        }

        @Override
        public void onCallConnected() {
            self().post(() -> AbsCallActor.this.onCallConnected());
        }

        @Override
        public void onCallEnabled() {
            self().post(() -> AbsCallActor.this.onCallEnabled());
        }

        @Override
        public void onBusStopped() {
            self().post(() -> AbsCallActor.this.onBusStopped());
        }

        @Override
        public void onPeerConnectionStateChanged(long deviceId, boolean isAudioEnabled, boolean isVideoEnabled) {
            self().post(() -> AbsCallActor.this.onPeerConnectionStateChanged(deviceId, isAudioEnabled, isVideoEnabled));
        }

        @Override
        public void onStreamAdded(long deviceId, WebRTCMediaStream stream) {
            self().post(() -> AbsCallActor.this.onStreamAdded(deviceId, stream));
        }

        @Override
        public void onStreamRemoved(long deviceId, WebRTCMediaStream stream) {
            self().post(() -> AbsCallActor.this.onStreamRemoved(deviceId, stream));
        }

        @Override
        public void onPeerConnectionDisposed(long deviceId) {
            self().post(() -> AbsCallActor.this.onPeerConnectionDisposed(deviceId));
        }


        @Override
        public void onOwnStreamAdded(WebRTCMediaStream stream) {
            self().post(() -> AbsCallActor.this.onOwnStreamAdded(stream));
        }

        @Override
        public void onOwnStreamRemoved(WebRTCMediaStream stream) {
            self().post(() -> AbsCallActor.this.onOwnStreamRemoved(stream));
        }
    }
}
