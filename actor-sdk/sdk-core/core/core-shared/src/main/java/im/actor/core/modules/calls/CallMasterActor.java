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
import im.actor.core.modules.calls.entity.CallMember;
import im.actor.core.modules.calls.entity.CallMemberState;
import im.actor.core.modules.calls.entity.MasterCallMember;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.collections.ManagedList;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class CallMasterActor extends CallActor {

    private static final String TAG = "CallMasterActor";

    private final Peer peer;
    private ActorRef callManager;
    private CommandCallback<Long> callback;
    private long callId;
    private CallVM callVM;

    private ManagedList<MasterCallMember> members;
    private boolean isAnswered = false;

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

                // TODO: Possible race conditions when members changed during call initiation
                // Need to return explicit callers in response
                if (peer.getPeerType() == PeerType.GROUP) {
                    members = ManagedList.of(getGroup(peer.getPeerId()).getMembers())
                            .filter(new Predicate<GroupMember>() {
                                @Override
                                public boolean apply(GroupMember groupMember) {
                                    return groupMember.getUid() != myUid();
                                }
                            })
                            .map(MasterCallMember.FROM_MEMBER);
                } else if (peer.getPeerType() == PeerType.PRIVATE) {
                    members = ManagedList.of(new MasterCallMember(peer.getPeerId(), CallMemberState.RINGING));
                } else {
                    // Halt?
                }

                callId = responseDoCall.getCallId();
                callVM = spanNewOutgoingVM(responseDoCall.getCallId(), peer);
                callVM.getIsMuted().change(isMuted());
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
        // Searching for a member
        //
        MasterCallMember member = members
                .filter(MasterCallMember.PREDICATE(uid))
                .firstOrNull();
        if (member == null) {
            return;
        }

        //
        // Adding registered device
        //
        member.getDeviceId().add(deviceId);

        //
        // Update member state if necessary
        //
        if (member.getState() == CallMemberState.RINGING) {
            member.setState(CallMemberState.CONNECTING);
        }

        //
        // For every newly connected device notify who is king
        // in this call
        //
        sendSignalingMessage(uid, deviceId, new ApiSwitchMaster());

        //
        // Update Members State
        //
        updateMembers();
    }

    @Override
    public void onDeviceDisconnected(final int uid, final long deviceId) {

        //
        // Searching for connected device
        //
        MasterCallMember member = members
                .filter(MasterCallMember.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (member == null) {
            return;
        }

        //
        // Remove device and if necessary remove from group
        //
        member.getDeviceId().remove(deviceId);
        if (member.getDeviceId().size() == 0) {
            members.remove(member);
        }

        //
        // If there are no members left - stop call
        //
        if (members.isEmpty()) {
            shutdown();
            return;
        }

        //
        // Update Members State
        //
        updateMembers();
    }

    @Override
    public void onStreamAdded(int uid, long deviceId, WebRTCMediaStream stream) {

        //
        // Changing State to IN_PROGRESS once first stream appear
        //
        if (!isAnswered) {
            isAnswered = true;
            callVM.getState().change(CallState.IN_PROGRESS);
            callManager.send(new CallManagerActor.OnCallAnswered(callId));
        }
    }

    public void onCallAnswered(int uid, long deviceId) {

        //
        // Searching for suitable Call Member
        //
        MasterCallMember callMember = members
                .filter(MasterCallMember.PREDICATE(uid))
                .firstOrNull();
        if (callMember == null) {
            return;
        }

        //
        // If already connected: ignore message
        //
//        if (callMember.getDeviceId().contains(deviceId)) {
//            return;
//        }

        //
        // Establishing connection
        //
        getPeer(uid, deviceId).send(new PeerConnectionActor.OnOfferNeeded());
        for (MasterCallMember member : members) {
            for (long devId : member.getDeviceId()) {
                sendSignalingMessage(member.getUid(), devId, new ApiNeedOffer(uid, deviceId));
            }
        }

        //
        // Adding new device
        //
        callMember.getDeviceId().add(deviceId);

        //
        // Update Member State
        //
        callMember.setState(CallMemberState.CONNECTED);

        //
        // Update Members State
        //
        updateMembers();
    }

    public void onCallRejected(int uid, long deviceId) {

        //
        // Searching for suitable Call Member
        //
        MasterCallMember callMember = members
                .filter(MasterCallMember.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (callMember == null) {
            return;
        }

        callMember.getDeviceId().remove(deviceId);

        if (callMember.getDeviceId().size() == 0) {
            members.remove(callMember);
        }

        if (members.isEmpty()) {
            shutdown();
            return;
        }

        //
        // Update Members State
        //
        updateMembers();
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


    private void updateMembers() {

        //
        // Update Calls VM
        //
        ArrayList<im.actor.core.viewmodel.CallMember> callMembers = new ArrayList<>();
        for (CallMember m : members) {
            im.actor.core.viewmodel.CallMemberState state;
            switch (m.getState()) {
                case RINGING:
                    state = im.actor.core.viewmodel.CallMemberState.CALLING;
                    break;
                case CONNECTED:
                    state = im.actor.core.viewmodel.CallMemberState.IN_PROGRESS;
                    break;
                case CONNECTING:
                default:
                    state = im.actor.core.viewmodel.CallMemberState.CALLING_REACHED;
            }
            callMembers.add(new im.actor.core.viewmodel.CallMember(m.getUid(), state));
        }
        callVM.getMembers().change(callMembers);

        //
        // Broadcast new members
        //
        sendSignalingMessage(createMembersChanged());
    }

    private ApiMembersChanged createMembersChanged() {
        ArrayList<ApiCallMember> callMembers = new ArrayList<>();
        for (CallMember m : members) {
            callMembers.add(new ApiCallMember(m.getUid(), 0, m.getState().toApiState()));
        }
        return new ApiMembersChanged(callMembers);
    }

    //
    // Messages handling
    //

    @Override
    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
        if (signaling instanceof ApiAnswerCall) {
            onCallAnswered(fromUid, fromDeviceId);
        } else if (signaling instanceof ApiRejectCall) {
            onCallRejected(fromUid, fromDeviceId);
        } else {
            super.onSignalingMessage(fromUid, fromDeviceId, signaling);
        }
    }
}