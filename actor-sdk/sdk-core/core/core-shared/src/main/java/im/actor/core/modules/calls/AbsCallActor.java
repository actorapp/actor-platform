package im.actor.core.modules.calls;

import java.util.List;

import im.actor.core.api.ApiCallMember;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.bus.CallBusActor;
import im.actor.core.modules.calls.bus.CallBusCallback;
import im.actor.core.modules.calls.bus.CallBusInt;
import im.actor.core.modules.calls.peers.PeerCallInt;
import im.actor.core.modules.calls.peers.PeerSettings;
import im.actor.core.modules.calls.peers.PeerState;
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
        public void onPeerStateChanged(final int uid, final long deviceId, final PeerState state) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    AbsCallActor.this.onPeerStateChanged(uid, deviceId, state);
                }
            });
        }
    }
}
