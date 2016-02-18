package im.actor.core.modules.calls;

import java.util.ArrayList;

import im.actor.core.api.ApiAnswerCall;
import im.actor.core.api.ApiCallMember;
import im.actor.core.api.ApiMembersChanged;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiRejectCall;
import im.actor.core.api.ApiSwitchMaster;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.entity.MasterCallDevice;
import im.actor.core.modules.calls.entity.MasterCallMemberState;
import im.actor.core.modules.calls.entity.MasterCallManager;
import im.actor.core.modules.calls.entity.MasterCallMember;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallMemberState;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class CallMasterActor extends CallActor {

    private static final String TAG = "CallMasterActor";

    private final Peer peer;

    private ActorRef callManager;
    private CommandCallback<Long> callback;
    private long callId;
    private CallVM callVM;

    private MasterCallManager state;

    public CallMasterActor(Peer peer, ModuleContext context, CommandCallback<Long> callback) {
        super(context);
        this.callback = callback;
        this.peer = peer;
    }

    @Override
    public void preStart() {
        super.preStart();
        callManager = context().getCallsModule().getCallManager();
        createBus();
    }

    @Override
    public void onBusCreated() {
        api(new RequestDoCall(buidOutPeer(peer), getBusId())).then(new Consumer<ResponseDoCall>() {
            @Override
            public void apply(ResponseDoCall responseDoCall) {

                //
                // Initialization of Call State
                //
                // TODO: Possible race conditions when members changed during call initiation
                // Need to return explicit callers in response
                state = new MasterCallManager();
                if (peer.getPeerType() == PeerType.GROUP) {
                    for (GroupMember gm : getGroup(peer.getPeerId()).getMembers()) {
                        if (gm.getUid() != myUid()) {
                            state.addMember(gm.getUid(), MasterCallMemberState.RINGING);
                        }
                    }
                } else if (peer.getPeerType() == PeerType.PRIVATE) {
                    state.addMember(peer.getPeerId(), MasterCallMemberState.RINGING);
                } else {
                    throw new RuntimeException("Unsupported Peer Type group");
                }

                //
                // Initialization of CallVM
                //
                callId = responseDoCall.getCallId();
                callVM = spanNewOutgoingVM(responseDoCall.getCallId(), peer);
                callVM.getIsMuted().change(isMuted());

                //
                // Notifying about successful call creation
                //
                callManager.send(new CallManagerActor.DoCallComplete(responseDoCall.getCallId()), self());
                callback.onResult(responseDoCall.getCallId());
                callback = null;
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                callback.onError(e);
                callback = null;
                dispose();
            }
        }).done(self());
    }

    @Override
    public void onDeviceConnected(int uid, long deviceId) {

        //
        // For each valid UID and DeviceID
        //
        if (state.onDeviceConnected(uid, deviceId)) {

            //
            // Notify who is king in this call
            //
            sendSignalingMessage(uid, deviceId, new ApiSwitchMaster());

            //
            // Update State
            //
            updateCallVMState();
            updateMembers();
        }
    }

    @Override
    public void onDeviceDisconnected(final int uid, final long deviceId) {

        //
        // For each valid UID and DeviceID
        //
        if (state.onDeviceDisconnected(uid, deviceId)) {

            //
            // Disconnecting peer connection
            //
            disconnectPeer(uid, deviceId);

            //
            // Update State
            //
            updateCallVMState();
            updateMembers();
        }
    }

    public void onDeviceAnswered(int uid, long deviceId) {

        //
        // For each valid UID and DeviceID
        //
        if (state.onDeviceAnswered(uid, deviceId)) {

            //
            // Starting connection to this device
            //
            getPeer(uid, deviceId).send(new PeerConnectionActor.OnOfferNeeded());
            for (MasterCallDevice device : state.getConnectedDevices()) {
                sendSignalingMessage(device.getUid(), device.getDeviceId(), new ApiNeedOffer(uid, deviceId));
            }

            //
            // Update State
            //
            updateCallVMState();
            updateMembers();
        }
    }

    public void onDeviceRejected(int uid, long deviceId) {

        //
        // For each valid UID and DeviceID
        //
        if (state.onDeviceRejected(uid, deviceId)) {

            //
            // Update State
            //
            updateCallVMState();
            updateMembers();
        }
    }

    @Override
    public void onStreamAdded(int uid, long deviceId, WebRTCMediaStream stream) {

        //
        // Changing State to IN_PROGRESS once first stream appear
        //

        if (state.onDeviceStreamAdded(uid, deviceId)) {

            //
            // Notify Call Manager for stopping call beeping
            //
            callManager.send(new CallManagerActor.OnCallAnswered(callId));

            //
            // Update State
            //
            updateCallVMState();
            updateMembers();
        }
    }


    @Override
    public void onMute() {
        super.onMute();

        //
        // Update CallVM state. Actual Muting is performed in super class.
        //
        if (callVM != null) {
            callVM.getIsMuted().change(true);
        }
    }

    @Override
    public void onUnmute() {
        super.onUnmute();

        //
        // Update CallVM state. Actual Muting is performed in super class.
        //
        if (callVM != null) {
            callVM.getIsMuted().change(false);
        }
    }

    @Override
    public void onBusStopped() {
        super.onBusStopped();

        //
        // Notify Creation callback if needed
        //
        if (callback != null) {
            callback.onError(new RuntimeException("Internal Error"));
        }

        //
        // EventBus stopped = call ended.
        // Send notification to CallVM and CallManager.
        //
        if (callVM != null) {
            callVM.getState().change(CallState.ENDED);
            callManager.send(new CallManagerActor.OnCallEnded(callId));
        }
    }


    private void updateCallVMState() {

        Log.d(TAG, "Call State:\n" + state);

        //
        // Do nothing if call is ended
        //
        if (callVM.getState().get() == CallState.ENDED) {
            return;
        }

        //
        // If All members ended call then end call
        //
        if (state.getConnectedMembers()
                .isAll(MasterCallMember.IS_ENDED)) {
            shutdown();
            return;
        }

        //
        // If Any member is in progress then call is in progress
        //
        if (state.getConnectedMembers()
                .isAny(MasterCallMember.IS_IN_PROGRESS)) {
            callVM.getState().change(CallState.IN_PROGRESS);
            return;
        }


        if (state.getConnectedMembers()
                .isAny(MasterCallMember.IS_CONNECTING)) {
            callVM.getState().change(CallState.CONNECTING);
            return;
        }

        // TODO: Connecting States
    }


    private void updateMembers() {

        //
        // Update Calls VM
        //
        ArrayList<im.actor.core.viewmodel.CallMember> callMembers = new ArrayList<>();
        for (MasterCallMember callMember : state.getConnectedMembers()) {
            CallMemberState callMemberState;
            switch (callMember.getState()) {
                case RINGING_REACHED:
                    callMemberState = CallMemberState.RINGING_REACHED;
                    break;
                case RINGING:
                    callMemberState = CallMemberState.RINGING;
                    break;
                case CONNECTING:
                    callMemberState = CallMemberState.CONNECTING;
                    break;
                case IN_PROGRESS:
                    callMemberState = CallMemberState.IN_PROGRESS;
                    break;
                default:
                case ENDED:
                    callMemberState = CallMemberState.ENDED;
                    break;
            }
            callMembers.add(new CallMember(callMember.getUid(), callMemberState));
        }
        callVM.getMembers().change(callMembers);

        //
        // Broadcast new members
        //
        // sendSignalingMessage(createMembersChanged());
    }

    private ApiMembersChanged createMembersChanged() {
        ArrayList<ApiCallMember> callMembers = new ArrayList<>();
//        for (CallMember m : members) {
//            callMembers.add(new ApiCallMember(m.getUid(), 0, m.getState().toApiState()));
//        }
        return new ApiMembersChanged(callMembers);
    }

    //
    // Messages handling
    //

    @Override
    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
        if (signaling instanceof ApiAnswerCall) {
            onDeviceAnswered(fromUid, fromDeviceId);
        } else if (signaling instanceof ApiRejectCall) {
            onDeviceRejected(fromUid, fromDeviceId);
        } else {
            super.onSignalingMessage(fromUid, fromDeviceId, signaling);
        }
    }
}