package im.actor.core.modules.calls;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.DeviceCategory;
import im.actor.core.api.ApiAnswer;
import im.actor.core.api.ApiCandidate;
import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiPeerSettings;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.entity.Group;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.entity.InvalidTransactionException;
// import im.actor.core.modules.calls.entity.PeerCollection;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallMemberState;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class CallActor extends EventBusActor {
    public CallActor(ModuleContext context) {
        super(context);
    }

//    private static final String TAG = "CallActor";
//
//    private PeerCollection peerCollection;
//    private HashMap<Long, CallVM> callModels;
//    private ApiPeerSettings peerSettings;
//
//    public CallActor(ModuleContext context) {
//        super(context);
//    }
//
//    @Override
//    public void preStart() {
//        super.preStart();
//        callModels = context().getCallsModule().getCallModels();
//        peerCollection = new PeerCollection(self(), context());
//        boolean isMobile = config().getDeviceCategory() == DeviceCategory.MOBILE ||
//                config().getDeviceCategory() == DeviceCategory.TABLET;
//        peerSettings = new ApiPeerSettings(true, isMobile, false, true);
//    }
//
//    public PeerCollection getPeerCollection() {
//        return peerCollection;
//    }
//
//    public ApiPeerSettings getPeerSettings() {
//        return peerSettings;
//    }
//
//    //
//    // Call Model helpers
//    //
//    public CallVM spawnNewVM(long callId, Peer peer, boolean isOutgoing, ArrayList<CallMember> members, CallState callState) {
//        CallVM callVM = new CallVM(callId, peer, isOutgoing, members, callState);
//        synchronized (callModels) {
//            callModels.put(callId, callVM);
//        }
//        return callVM;
//    }
//
//    public CallVM spanNewOutgoingVM(long callId, Peer peer) {
//        ArrayList<CallMember> members = new ArrayList<>();
//        if (peer.getPeerType() == PeerType.PRIVATE ||
//                peer.getPeerType() == PeerType.PRIVATE_ENCRYPTED) {
//            members.add(new CallMember(peer.getPeerId(), CallMemberState.RINGING));
//        } else if (peer.getPeerType() == PeerType.GROUP) {
//            Group g = getGroup(peer.getPeerId());
//            for (GroupMember gm : g.getMembers()) {
//                if (gm.getUid() != myUid()) {
//                    members.add(new CallMember(gm.getUid(), CallMemberState.RINGING));
//                }
//            }
//        }
//        return spawnNewVM(callId, peer, true, members, CallState.CALLING);
//    }
//
//    //
//    // Nodes
//    //
//
//    public void onNodeConnected(int uid, long deviceId) throws InvalidTransactionException {
//
//    }
//
//    public void onNodeDisconnected(int uid, long deviceId) throws InvalidTransactionException {
//
//    }
//
//
//    //
//    // Signaling Wrappers
//    //
//
//    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
//        if (signaling instanceof ApiOffer) {
//            ApiOffer offer = (ApiOffer) signaling;
//            peerCollection.getPeer(fromUid, fromDeviceId)
//                    .send(new PeerConnectionActor.OnOffer(offer.getSdp()));
//        } else if (signaling instanceof ApiAnswer) {
//            ApiAnswer answer = (ApiAnswer) signaling;
//            peerCollection.getPeer(fromUid, fromDeviceId)
//                    .send(new PeerConnectionActor.OnAnswer(answer.getSdp()));
//        } else if (signaling instanceof ApiCandidate) {
//            ApiCandidate candidate = (ApiCandidate) signaling;
//            peerCollection.getPeer(fromUid, fromDeviceId)
//                    .send(new PeerConnectionActor.OnCandidate(candidate.getIndex(),
//                            candidate.getId(), candidate.getSdp()));
//        }
//    }
//
//    public void onStreamAdded(int uid, long deviceId, WebRTCMediaStream stream) throws InvalidTransactionException {
//
//    }
//
//    public void onStreamRemoved(int uid, long deviceId, WebRTCMediaStream stream) throws InvalidTransactionException {
//
//    }
//
//    //
//    // Send Signaling
//    //
//
//    public final void sendSignalingMessage(int uid, long deviceId, ApiWebRTCSignaling signaling) {
//        try {
//            sendMessage(uid, deviceId, signaling.buildContainer());
//        } catch (IOException e) {
//            e.printStackTrace();
//            // Ignore
//        }
//    }
//
//    public final void sendSignalingMessage(int uid, ApiWebRTCSignaling signaling) {
//        try {
//            sendMessage(uid, signaling.buildContainer());
//        } catch (IOException e) {
//            e.printStackTrace();
//            // Ignore
//        }
//    }
//
//    public final void sendSignalingMessage(ApiWebRTCSignaling signaling) {
//        try {
//            sendMessage(signaling.buildContainer());
//        } catch (IOException e) {
//            e.printStackTrace();
//            // Ignore
//        }
//    }
//
//    public void onMuteChanged(boolean value) {
//        peerCollection.setIsMuted(value);
//    }
//
//    //
//    // Call Ending
//    //
//
//    @Override
//    public void onBusStopped() {
//        super.onBusStopped();
//        peerCollection.stopAll();
//    }
//
//    public void doEndCall() {
//        shutdown();
//    }
//
//    //
//    // Messages
//    //
//
//    @Override
//    public final void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
//        if (senderId == null || senderDeviceId == null) {
//            return;
//        }
//
//        ApiWebRTCSignaling signaling;
//        try {
//            signaling = ApiWebRTCSignaling.fromBytes(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d(TAG, "onMessageReceived:ignoring");
//            return;
//        }
//
//        Log.d(TAG, "onMessageReceived: " + signaling);
//        onSignalingMessage(senderId, senderDeviceId, signaling);
//    }
//
//    @Override
//    public final void onDeviceConnected(int uid, long deviceId) {
//        super.onDeviceConnected(uid, deviceId);
//        try {
//            onNodeConnected(uid, deviceId);
//        } catch (InvalidTransactionException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onDeviceDisconnected(int uid, long deviceId) {
//        super.onDeviceDisconnected(uid, deviceId);
//        try {
//            onNodeDisconnected(uid, deviceId);
//        } catch (InvalidTransactionException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onReceive(Object message) {
//        if (message instanceof PeerConnectionActor.DoAnswer) {
//            PeerConnectionActor.DoAnswer answer = (PeerConnectionActor.DoAnswer) message;
//            sendSignalingMessage(answer.getUid(), answer.getDeviceId(),
//                    new ApiAnswer(0, answer.getSdp()));
//        } else if (message instanceof PeerConnectionActor.DoOffer) {
//            PeerConnectionActor.DoOffer offer = (PeerConnectionActor.DoOffer) message;
//            sendSignalingMessage(offer.getUid(), offer.getDeviceId(),
//                    new ApiOffer(0, offer.getSdp(), null));
//        } else if (message instanceof PeerConnectionActor.DoCandidate) {
//            PeerConnectionActor.DoCandidate candidate = (PeerConnectionActor.DoCandidate) message;
//            sendSignalingMessage(candidate.getUid(), candidate.getDeviceId(),
//                    new ApiCandidate(0, candidate.getIndex(), candidate.getId(), candidate.getSdp()));
//        } else if (message instanceof DoEndCall) {
//            doEndCall();
//        } else if (message instanceof PeerConnectionActor.OnStreamAdded) {
//            PeerConnectionActor.OnStreamAdded streamAdded = (PeerConnectionActor.OnStreamAdded) message;
//            try {
//                onStreamAdded(streamAdded.getUid(), streamAdded.getDeviceId(), streamAdded.getStream());
//            } catch (InvalidTransactionException e) {
//                e.printStackTrace();
//            }
//        } else if (message instanceof PeerConnectionActor.OnStreamRemoved) {
//            PeerConnectionActor.OnStreamRemoved streamRemoved = (PeerConnectionActor.OnStreamRemoved) message;
//            try {
//                onStreamRemoved(streamRemoved.getUid(), streamRemoved.getDeviceId(), streamRemoved.getStream());
//            } catch (InvalidTransactionException e) {
//                e.printStackTrace();
//            }
//        } else if (message instanceof Mute) {
//            onMuteChanged(true);
//        } else if (message instanceof Unmute) {
//            onMuteChanged(false);
//        } else {
//            super.onReceive(message);
//        }
//    }
//
//    public static class DoEndCall {
//
//    }
//
//    public static class Mute {
//
//    }
//
//    public static class Unmute {
//
//    }
}