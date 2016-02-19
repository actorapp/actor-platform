package im.actor.core.modules.calls;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiAdvertiseSelf;
import im.actor.core.api.ApiAnswerCall;
import im.actor.core.api.ApiCallMember;
import im.actor.core.api.ApiCallMemberStateHolder;
import im.actor.core.api.ApiMembersChanged;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiPeerSettings;
import im.actor.core.api.ApiRejectCall;
import im.actor.core.api.ApiSwitchMaster;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestGetCallInfo;
import im.actor.core.api.rpc.ResponseGetCallInfo;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallMemberState;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.webrtc.WebRTCMediaStream;

import static im.actor.core.modules.internal.messages.entity.EntityConverter.convert;

public class CallSlaveActor extends CallActor {

    private ActorRef callManager;
    private MasterNode masterNode;
    private long callId;
    private Peer peer;
    private CallVM callVM;

    public CallSlaveActor(long callId, ModuleContext context) {
        super(context);
        this.callId = callId;
    }

    @Override
    public void preStart() {
        super.preStart();
        callManager = context().getCallsModule().getCallManager();
        setIsSilentEnabled(true);
        api(new RequestGetCallInfo(callId)).then(new Consumer<ResponseGetCallInfo>() {
            @Override
            public void apply(final ResponseGetCallInfo responseGetCallInfo) {
                peer = convert(responseGetCallInfo.getPeer());
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
        callVM = spawnNewVM(callId, peer, false, new ArrayList<CallMember>(), CallState.CALLING);
        callVM.getIsMuted().change(isMuted());
    }

    public void onMasterNodeChanged(int fromUid, long fromDeviceId) {
        masterNode = new MasterNode(fromUid, fromDeviceId);

        //
        // Advertise own settings to call master
        //
        sendSignalingMessage(fromUid, fromDeviceId, new ApiAdvertiseSelf(getPeerSettings()));

        //
        // Notify UI only after successful master node information received
        //
        callManager.send(new CallManagerActor.IncomingCallReady(callId), self());
    }

    public void onMembersChanged(List<ApiCallMember> allMembers) {

        //
        // Handling Members
        //
        ArrayList<CallMember> members = new ArrayList<>();
        for (ApiCallMember apiCallMember : allMembers) {
            if (getUser(apiCallMember.getUserId()) == null) {
                continue;
            }
            if (apiCallMember.getUserId() == myUid()) {
                continue;
            }
            ApiCallMemberStateHolder stateHolder = apiCallMember.getState();
            CallMemberState state;
            switch (stateHolder.getState()) {
                case RINGING:
                    state = CallMemberState.RINGING;
                    break;
                case RINGING_REACHED:
                    state = CallMemberState.RINGING_REACHED;
                    break;
                case CONNECTING:
                    state = CallMemberState.CONNECTING;
                    break;
                case CONNECTED:
                    state = CallMemberState.IN_PROGRESS;
                    break;
                case ENDED:
                    state = CallMemberState.ENDED;
                    break;
                default:
                    if (stateHolder.fallbackIsRingingReached() != null && stateHolder.fallbackIsRingingReached()) {
                        state = CallMemberState.RINGING_REACHED;
                        break;
                    }
                    if (stateHolder.fallbackIsEnded() != null && stateHolder.fallbackIsEnded()) {
                        state = CallMemberState.ENDED;
                        break;
                    }
                    if (stateHolder.fallbackIsRinging() != null && stateHolder.fallbackIsRinging()) {
                        state = CallMemberState.RINGING;
                        break;
                    }

                    if (stateHolder.fallbackIsConnecting() != null && stateHolder.fallbackIsConnecting()) {
                        state = CallMemberState.CONNECTING;
                        break;
                    }

                    if (stateHolder.fallbackIsConnected() != null && stateHolder.fallbackIsConnected()) {
                        state = CallMemberState.IN_PROGRESS;
                        break;
                    }
                    state = CallMemberState.RINGING;
            }
            members.add(new CallMember(apiCallMember.getUserId(), state));
        }
        callVM.getMembers().change(members);
    }

    public void onNeedOffer(int destUid, long destDeviceId, Boolean isSilent, ApiPeerSettings peerSettings) {
        getPeer(destUid, destDeviceId).send(new PeerConnectionActor.OnOfferNeeded());
    }

    public void doAnswer() {
        callVM.getState().change(CallState.CONNECTING);
        sendSignalingMessage(masterNode.getUid(), masterNode.getDeviceId(), new ApiAnswerCall());
        unsilencePeers();
    }

    @Override
    public void onStreamAdded(int uid, long deviceId, WebRTCMediaStream stream) {
        if (uid != myUid() && callVM.getState().get() == CallState.CONNECTING) {
            callVM.getState().change(CallState.IN_PROGRESS);
        }
    }

    @Override
    public void doEndCall() {
        super.doEndCall();
        if (callVM != null) {
            callVM.getState().change(CallState.ENDED);
        }
        if (masterNode != null) {
            sendSignalingMessage(masterNode.getUid(), masterNode.getDeviceId(), new ApiRejectCall());
        }
    }

    @Override
    public void onBusStopped() {
        super.onBusStopped();

        if (callVM != null) {
            callVM.getState().change(CallState.ENDED);
        }
        callManager.send(new CallManagerActor.OnCallEnded(callId));
    }

    //
    // Messages
    //

    @Override
    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
        if (signaling instanceof ApiNeedOffer) {
            ApiNeedOffer needOffer = (ApiNeedOffer) signaling;
            onNeedOffer(needOffer.getUid(), needOffer.getDevice(), needOffer.isSilent(), needOffer.getPeerSettings());
        } else if (signaling instanceof ApiSwitchMaster) {
            onMasterNodeChanged(fromUid, fromDeviceId);
        } else if (signaling instanceof ApiMembersChanged) {
            onMembersChanged(((ApiMembersChanged) signaling).getAllMembers());
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
