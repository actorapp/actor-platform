package im.actor.core.modules.calls;

import java.util.List;

import im.actor.core.api.ApiAdvertiseSelf;
import im.actor.core.api.ApiAnswerCall;
import im.actor.core.api.ApiCallMember;
import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.ResponseGetCallInfo;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.bus.CallBusActor;
import im.actor.core.modules.calls.bus.CallBusCallbackSlave;
import im.actor.core.modules.calls.bus.CallBusInt;
import im.actor.core.modules.calls.peers.PeerCallInt;
import im.actor.core.modules.calls.peers.PeerSettings;
import im.actor.core.util.ModuleActor;
import im.actor.core.viewmodel.CallVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;

import static im.actor.core.modules.internal.messages.entity.EntityConverter.convert;

public class CallActor extends ModuleActor implements CallBusCallbackSlave {

    private static final String TAG = "CallActor";

    private CallBusInt callBusInt;
    private PeerCallInt peerCallInt;
    private final ActorRef callManager;
    private final long callId;
    private final PeerSettings selfSettings;

    private Peer peer;
    private CallVM callVM;

    private boolean isMasterReady;
    private int masterUid;
    private long masterDeviceId;

    private boolean isConnected;
    private boolean isAnswered;
    private boolean isRejected;

    public CallActor(long callId, ModuleContext context) {
        super(context);

        this.callId = callId;
        this.callManager = context.getCallsModule().getCallManager();
        this.isAnswered = false;
        this.isConnected = false;
        this.selfSettings = new PeerSettings();
        this.selfSettings.setIsPreConnectionEnabled(true);
    }

    @Override
    public void preStart() {
        super.preStart();


        callBusInt = new CallBusInt(system().actorOf(getPath() + "/bus", new ActorCreator() {
            @Override
            public Actor create() {
                return new CallBusActor(new CallbackWrapper(), selfSettings, context());
            }
        }));
    }

    @Override
    public void onBusCreated(PeerCallInt peerCallInt) {
        this.peerCallInt = peerCallInt;

        api(new RequestGetCallInfo(callId)).then(new Consumer<ResponseGetCallInfo>() {
            @Override
            public void apply(final ResponseGetCallInfo responseGetCallInfo) {
                peer = convert(responseGetCallInfo.getPeer());
                callBusInt.joinBus(responseGetCallInfo.getEventBusId());
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {

            }
        }).done(self());
    }

    @Override
    public void onBusStarted(String busId) {

    }

    @Override
    public void onMasterSwitched(int uid, long deviceId) {
        if (isMasterReady) {
            return;
        }

        Log.d(TAG, "onMasterSwitched");

        isMasterReady = true;
        masterUid = uid;
        masterDeviceId = deviceId;
        callBusInt.sendSignal(masterUid, masterDeviceId, new ApiAdvertiseSelf(selfSettings.toApi()));
        unstashAll();

        schedule(new AnswerCall(), 1000);
    }

    @Override
    public void onMembersChanged(List<ApiCallMember> members) {
        Log.d(TAG, "onMembersChanged");

    }

    public void onAnswerCall() {
        callBusInt.sendSignal(masterUid, masterDeviceId, new ApiAnswerCall());
        peerCallInt.onOwnStarted();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof AnswerCall) {
            if (!isMasterReady) {
                stash();
                return;
            }
            onAnswerCall();
        } else {
            super.onReceive(message);
        }
    }

    public static class AnswerCall {

    }

    public class CallbackWrapper implements CallBusCallbackSlave {

        @Override
        public void onMasterSwitched(final int uid, final long deviceId) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    CallActor.this.onMasterSwitched(uid, deviceId);
                }
            });
        }

        @Override
        public void onMembersChanged(final List<ApiCallMember> members) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    CallActor.this.onMembersChanged(members);
                }
            });
        }

        @Override
        public void onBusCreated(final PeerCallInt peerCallInt) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    CallActor.this.onBusCreated(peerCallInt);
                }
            });
        }

        @Override
        public void onBusStarted(final String busId) {
            self().send(new Runnable() {
                @Override
                public void run() {
                    CallActor.this.onBusStarted(busId);
                }
            });
        }
    }

//    @Override
//    public void onSignalingStarted() {
//
//    }
//
//    @Override
//    public void onSignal(int uid, long deviceId, ApiWebRTCSignaling signal) {
//        if (signal instanceof ApiSwitchMaster) {
//
//        }
//        Log.d(TAG, "onSignal: " + signal);
//    }
//
//    @Override
//    public void onSignalingStopped() {
//
//    }
}
