package im.actor.core.modules.calls.peers;

import java.util.List;

import im.actor.core.api.ApiCallMember;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.CallViewModels;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;

public abstract class AbsCallActor extends EventBusActor implements CallBusCallback {

    protected final PeerSettings selfSettings;
    protected final CallViewModels callViewModels;
    protected final ActorRef callManager;
    protected CallBusInt callBus;
    protected PeerCallInt peerCall;

    public AbsCallActor(ModuleContext context) {
        super(context);

        this.callManager = context.getCallsModule().getCallManager();
        this.callViewModels = context().getCallsModule().getCallViewModels();
        this.selfSettings = new PeerSettings();
        this.selfSettings.setIsPreConnectionEnabled(true);
    }

    @Override
    public void preStart() {
        super.preStart();
        callBus = new CallBusInt(system().actorOf(getPath() + "/bus", new ActorCreator() {
            @Override
            public Actor create() {
                return new CallBusActor(new CallBusCallbackWrapper(), selfSettings, context());
            }
        }));
    }

    @Override
    public final void onBusCreated(PeerCallInt peerCallInt) {
        this.peerCall = peerCallInt;
        callPreStart();
    }

    public void callPreStart() {

    }

    @Override
    public void onBusStarted(String busId) {

    }

    @Override
    public void onMembersChanged(List<ApiCallMember> members) {

    }

    @Override
    public void onPeerStateChanged(int uid, long deviceId, PeerState state) {

    }

    @Override
    public void onPeerConnected(int uid, long deviceId) {

    }

    @Override
    public void onAnswered(int uid, long deviceId) {

    }

    public void onMuteChanged(boolean isMuted) {
        peerCall.onMuteChanged(isMuted);
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
        public void onBusCreated(final PeerCallInt peerCallInt) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    AbsCallActor.this.onBusCreated(peerCallInt);
                }
            });
        }

        @Override
        public void onBusStarted(final String busId) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    AbsCallActor.this.onBusStarted(busId);
                }
            });
        }

        @Override
        public void onMembersChanged(final List<ApiCallMember> members) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    AbsCallActor.this.onMembersChanged(members);
                }
            });
        }

        @Override
        public void onPeerConnected(final int uid, final long deviceId) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    AbsCallActor.this.onPeerConnected(uid, deviceId);
                }
            });
        }

        @Override
        public void onPeerStateChanged(final int uid, final long deviceId, final PeerState state) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    AbsCallActor.this.onPeerStateChanged(uid, deviceId, state);
                }
            });
        }

        @Override
        public void onAnswered(final int uid, final long deviceId) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    AbsCallActor.this.onAnswered(uid, deviceId);
                }
            });
        }
    }
}
