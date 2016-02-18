package im.actor.core.modules.calls;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiAnswerCall;
import im.actor.core.api.ApiCallMember;
import im.actor.core.api.ApiCallMemberStateHolder;
import im.actor.core.api.ApiMembersChanged;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiSwitchMaster;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.entity.CallMember;
import im.actor.core.modules.calls.entity.CallMemberState;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class CallMasterActor extends CallActor {

    private static final String TAG = "CallMasterActor";

    private final Peer peer;
    private ActorRef callManager;
    private CommandCallback<Long> callback;
    private ArrayList<ConnectedHolder> connectedDevices = new ArrayList<>();
    private CallVM callVM;
    private long callId;
    private ArrayList<CallMember> members = new ArrayList<>();
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
        // For every newly connected device notify who is king
        // in this call
        //
        sendSignalingMessage(uid, deviceId, new ApiSwitchMaster());

        //
        // Pending Members
        //
        boolean found = false;
        for (CallMember m : members) {
            if (m.getUid() == uid) {
                m.setState(CallMemberState.CONNECTING);
                found = true;
                break;
            }
        }
        if (!found) {
            members.add(new CallMember(uid, CallMemberState.CONNECTING));
        }

        //
        // Notify everyone about new member
        //
        sendSignalingMessage(createMembersChanged());
    }

    @Override
    public void onDeviceDisconnected(int uid, long deviceId) {

        //
        // Removing connected device. If it is was the last - stop call.
        //
        ConnectedHolder connectedHolder = new ConnectedHolder(uid, deviceId);
        if (connectedDevices.contains(connectedHolder)) {
            connectedDevices.remove(connectedHolder);
            if (connectedDevices.size() == 0) {
                shutdown();
                return;
            }

            //
            // Removing active member
            //
            for (CallMember m : members) {
                if (m.getUid() == uid) {
                    members.remove(m);
                    break;
                }
            }

            //
            // Notify everyone about members changed
            //
            sendSignalingMessage(createMembersChanged());
        }
    }

    @Override
    public void onStreamAdded(int uid, long deviceId, WebRTCMediaStream stream) {

        //
        // Changing State to IN_PROGRESS once first stream appear
        //
        if (callVM.getState().get() == CallState.CALLING_OUTGOING) {
            callVM.getState().change(CallState.IN_PROGRESS);
        }

        if (!isAnswered) {
            isAnswered = true;
            callManager.send(new CallManagerActor.AnswerCall(callId));
        }
    }

    @Override
    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
        if (signaling instanceof ApiAnswerCall) {
            ConnectedHolder connectedHolder = new ConnectedHolder(fromUid, fromDeviceId);
            if (connectedDevices.contains(connectedHolder)) {
                return;
            }
            getPeer(fromUid, fromDeviceId).send(new PeerConnectionActor.OnOfferNeeded());
            for (ConnectedHolder c : connectedDevices) {
                sendSignalingMessage(c.uid, c.deviceId, new ApiNeedOffer(fromUid, fromDeviceId));
            }
            connectedDevices.add(connectedHolder);

            for (CallMember m : members) {
                if (m.getUid() == fromUid) {
                    m.setState(CallMemberState.CONNECTED);
                    break;
                }
            }
            sendSignalingMessage(createMembersChanged());
        } else {
            super.onSignalingMessage(fromUid, fromDeviceId, signaling);
        }
    }

    @Override
    public void onMute() {
        super.onMute();
        if (callVM != null) {
            callVM.getIsMuted().change(true);
        }
    }

    @Override
    public void onUnmute() {
        super.onUnmute();
        if (callVM != null) {
            callVM.getIsMuted().change(false);
        }
    }

    @Override
    public void onBusDisposed() {
        super.onBusDisposed();
        if (callback != null) {
            callback.onError(new RuntimeException("Internal Error"));
        }
    }

    @Override
    public void onBusStopped() {
        super.onBusStopped();
        callVM.getState().change(CallState.ENDED);
        callManager.send(new CallManagerActor.OnCallEnded(callId));
    }

    private ApiMembersChanged createMembersChanged() {
        ArrayList<ApiCallMember> callMembers = new ArrayList<>();
        for (CallMember m : members) {
            callMembers.add(new ApiCallMember(m.getUid(), 0, m.getState().toApiState()));
        }
        return new ApiMembersChanged(callMembers);
    }

    private static class ConnectedHolder {

        private int uid;
        private long deviceId;

        public ConnectedHolder(int uid, long deviceId) {
            this.uid = uid;
            this.deviceId = deviceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConnectedHolder that = (ConnectedHolder) o;

            if (uid != that.uid) return false;
            return deviceId == that.deviceId;

        }

        @Override
        public int hashCode() {
            int result = uid;
            result = 31 * result + (int) (deviceId ^ (deviceId >>> 32));
            return result;
        }
    }

    private static class ConnectedUser {
        private int uid;

    }

    private enum UserState {
    }
}