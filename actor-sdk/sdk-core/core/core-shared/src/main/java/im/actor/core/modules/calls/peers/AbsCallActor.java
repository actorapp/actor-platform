package im.actor.core.modules.calls.peers;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.CallViewModels;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.WebRTC;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;

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

    public void onMuteChanged(boolean isMuted) {
        callBus.changeMute(isMuted);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof MuteChanged) {
            onMuteChanged(((MuteChanged) message).isMuted());
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

    //
    // Wrapper
    //

    private class CallBusCallbackWrapper implements CallBusCallback {

        @Override
        public void onBusStarted(final String busId) {
            self().send((Runnable) () -> AbsCallActor.this.onBusStarted(busId));
        }

        @Override
        public void onCallConnected() {
            self().send((Runnable) () -> AbsCallActor.this.onCallConnected());
        }

        @Override
        public void onCallEnabled() {
            self().send((Runnable) () -> AbsCallActor.this.onCallEnabled());
        }

        @Override
        public void onBusStopped() {
            self().send((Runnable) () -> AbsCallActor.this.onBusStopped());
        }
    }
}
