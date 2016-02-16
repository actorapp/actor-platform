package im.actor.core.modules.calls;

import java.util.ArrayList;

import im.actor.core.api.ApiAnswerCall;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiSwitchMaster;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.ResponseGetCallInfo;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallState;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;

import static im.actor.core.modules.internal.messages.entity.EntityConverter.convert;

public class CallSlaveActor extends CallActor {

    private ActorRef callManager;
    private MasterNode masterNode;
    private boolean isAnswerPending = false;
    private long callId;

    public CallSlaveActor(long callId, ModuleContext context) {
        super(context);
        this.callId = callId;
    }

    @Override
    public void preStart() {
        super.preStart();
        callManager = context().getCallsModule().getCallManager();
        api(new RequestGetCallInfo(callId)).then(new Consumer<ResponseGetCallInfo>() {
            @Override
            public void apply(final ResponseGetCallInfo responseGetCallInfo) {
                spawnNewVM(callId, convert(responseGetCallInfo.getPeer()),
                        new ArrayList<CallMember>(), CallState.CALLING_INCOMING);
                callManager.send(new CallManagerActor.IncomingCallReady(callId), self());
                joinBus(responseGetCallInfo.getEventBusId());
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                shutdown();
            }
        }).done(self());
    }

    public void onMasterNodeChanged(int fromUid, long fromDeviceId) {
        masterNode = new MasterNode(fromUid, fromDeviceId);
        if (isAnswerPending) {
            isAnswerPending = false;
            sendSignalingMessage(masterNode.getUid(), masterNode.getDeviceId(), new ApiAnswerCall());
        }

        schedule(new DoAnswer(), 5000);
    }

    public void onNeedOffer(int destUid, long destDeviceId) {
        getPeer(destUid, destDeviceId).send(new PeerConnectionActor.OnOfferNeeded());
    }

    public void doAnswer() {
        if (masterNode == null) {
            isAnswerPending = true;
        } else {
            sendSignalingMessage(masterNode.getUid(), masterNode.getDeviceId(), new ApiAnswerCall());
        }
    }


    //
    // Messages
    //

    @Override
    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
        if (signaling instanceof ApiNeedOffer) {
            ApiNeedOffer needOffer = (ApiNeedOffer) signaling;
            onNeedOffer(needOffer.getUid(), needOffer.getDevice());
        } else if (signaling instanceof ApiSwitchMaster) {
            onMasterNodeChanged(fromUid, fromDeviceId);
        } else {
            super.onSignalingMessage(fromUid, fromDeviceId, signaling);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof DoAnswer) {
            doAnswer();
        } else {
            super.onReceive(message);
        }
    }

    private class MasterNode {

        private int uid;
        private long deviceId;

        public MasterNode(int uid, long deviceId) {
            this.uid = uid;
            this.deviceId = deviceId;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }
    }

    public static class DoAnswer {

    }
}
