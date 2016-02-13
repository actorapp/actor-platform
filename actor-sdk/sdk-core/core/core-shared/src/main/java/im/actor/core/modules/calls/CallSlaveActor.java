package im.actor.core.modules.calls;

import im.actor.core.api.ApiAnswerCall;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiSwitchMaster;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.ResponseGetCallInfo;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.function.Consumer;

public class CallSlaveActor extends CallActor {

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
        api(new RequestGetCallInfo(callId)).then(new Consumer<ResponseGetCallInfo>() {
            @Override
            public void apply(final ResponseGetCallInfo responseGetCallInfo) {
                joinBus(responseGetCallInfo.getEventBusId());
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                shutdown();
            }
        }).done(self());
    }

    @Override
    public void onBusStarted() {
        super.onBusStarted();
    }

    public void onMasterNodeChanged(int fromUid, long fromDeviceId) {
        masterNode = new MasterNode(fromUid, fromDeviceId);
        if (isAnswerPending) {
            isAnswerPending = false;
            sendSignalingMessage(masterNode.getUid(), masterNode.getDeviceId(), new ApiAnswerCall());
        }
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
