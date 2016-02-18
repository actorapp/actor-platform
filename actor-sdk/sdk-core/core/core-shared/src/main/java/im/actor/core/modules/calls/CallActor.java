package im.actor.core.modules.calls;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiAnswer;
import im.actor.core.api.ApiCandidate;
import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.entity.Group;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallMemberState;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class CallActor extends EventBusActor {

    private static final String TAG = "CallActor";

    private HashMap<Integer, HashMap<Long, ActorRef>> peerConnections = new HashMap<>();
    private HashMap<Long, CallVM> callModels;
    private boolean isMuted = false;

    public CallActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();
        callModels = context().getCallsModule().getCallModels();
    }

    public boolean isMuted() {
        return isMuted;
    }

    //
    // Call Model helpers
    //
    public CallVM spawnNewVM(long callId, Peer peer, ArrayList<CallMember> members, CallState callState) {
        CallVM callVM = new CallVM(callId, peer, members, callState);
        synchronized (callModels) {
            callModels.put(callId, callVM);
        }
        return callVM;
    }

    public CallVM spanNewOutgoingVM(long callId, Peer peer) {
        ArrayList<CallMember> members = new ArrayList<>();
        if (peer.getPeerType() == PeerType.PRIVATE ||
                peer.getPeerType() == PeerType.PRIVATE_ENCRYPTED) {
            members.add(new CallMember(peer.getPeerId(), CallMemberState.CALLING));
        } else if (peer.getPeerType() == PeerType.GROUP) {
            Group g = getGroup(peer.getPeerId());
            for (GroupMember gm : g.getMembers()) {
                if (gm.getUid() != myUid()) {
                    members.add(new CallMember(gm.getUid(), CallMemberState.CALLING));
                }
            }
        }
        return spawnNewVM(callId, peer, members, CallState.CALLING_OUTGOING);
    }

    //
    // Signaling Wrappers
    //

    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
        if (signaling instanceof ApiOffer) {
            ApiOffer offer = (ApiOffer) signaling;
            getPeer(fromUid, fromDeviceId).send(new PeerConnectionActor.OnOffer(offer.getSdp()));
        } else if (signaling instanceof ApiAnswer) {
            ApiAnswer answer = (ApiAnswer) signaling;
            getPeer(fromUid, fromDeviceId).send(new PeerConnectionActor.OnAnswer(answer.getSdp()));
        } else if (signaling instanceof ApiCandidate) {
            ApiCandidate candidate = (ApiCandidate) signaling;
            getPeer(fromUid, fromDeviceId).send(new PeerConnectionActor.OnCandidate(candidate.getIndex(),
                    candidate.getId(), candidate.getSdp()));
        }
    }

    public void onStreamAdded(int uid, long deviceId, WebRTCMediaStream stream) {

    }

    public void onStreamRemoved(int uid, long deviceId, WebRTCMediaStream stream) {

    }

    public final void sendSignalingMessage(int uid, long deviceId, ApiWebRTCSignaling signaling) {
        try {
            sendMessage(uid, deviceId, signaling.buildContainer());
        } catch (IOException e) {
            e.printStackTrace();
            // Ignore
        }
    }

    public final void sendSignalingMessage(int uid, ApiWebRTCSignaling signaling) {
        try {
            sendMessage(uid, signaling.buildContainer());
        } catch (IOException e) {
            e.printStackTrace();
            // Ignore
        }
    }

    public final void sendSignalingMessage(ApiWebRTCSignaling signaling) {
        try {
            sendMessage(signaling.buildContainer());
        } catch (IOException e) {
            e.printStackTrace();
            // Ignore
        }
    }

    public void onMute() {
        if (isMuted) {
            return;
        }
        isMuted = true;

        for (int uid : peerConnections.keySet()) {
            HashMap<Long, ActorRef> peers = peerConnections.get(uid);
            for (ActorRef p : peers.values()) {
                p.send(new PeerConnectionActor.DoMute());
            }
        }
    }

    public void onUnmute() {
        if (!isMuted) {
            return;
        }
        isMuted = false;

        for (int uid : peerConnections.keySet()) {
            HashMap<Long, ActorRef> peers = peerConnections.get(uid);
            for (ActorRef p : peers.values()) {
                p.send(new PeerConnectionActor.DoUnmute());
            }
        }
    }

    @Override
    public final void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
        if (senderId == null || senderDeviceId == null) {
            return;
        }

        ApiWebRTCSignaling signaling;
        try {
            signaling = ApiWebRTCSignaling.fromBytes(data);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onMessageReceived:ignoring");
            return;
        }

        Log.d(TAG, "onMessageReceived: " + signaling);
        onSignalingMessage(senderId, senderDeviceId, signaling);
    }

    @Override
    public void onBusStopped() {
        super.onBusStopped();
        for (int uid : peerConnections.keySet()) {
            HashMap<Long, ActorRef> peers = peerConnections.get(uid);
            for (ActorRef p : peers.values()) {
                p.send(new PeerConnectionActor.DoStop());
            }
        }
        peerConnections.clear();
    }

    public final void doEndCall() {
        shutdown();
    }

    protected ActorRef getPeer(int uid, long deviceId) {
        if (!peerConnections.containsKey(uid)) {
            peerConnections.put(uid, new HashMap<Long, ActorRef>());
        }
        HashMap<Long, ActorRef> refs = peerConnections.get(uid);
        if (refs.containsKey(deviceId)) {
            return refs.get(deviceId);
        }
        ActorRef ref = system().actorOf(getPath() + "/uid:" + uid + "/" + deviceId,
                PeerConnectionActor.CONSTRUCTOR(self(), uid, deviceId, isMuted, context()));
        refs.put(deviceId, ref);
        return ref;
    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof PeerConnectionActor.DoAnswer) {
            PeerConnectionActor.DoAnswer answer = (PeerConnectionActor.DoAnswer) message;
            sendSignalingMessage(answer.getUid(), answer.getDeviceId(),
                    new ApiAnswer(0, answer.getSdp()));
        } else if (message instanceof PeerConnectionActor.DoOffer) {
            PeerConnectionActor.DoOffer offer = (PeerConnectionActor.DoOffer) message;
            sendSignalingMessage(offer.getUid(), offer.getDeviceId(),
                    new ApiOffer(0, offer.getSdp()));
        } else if (message instanceof PeerConnectionActor.DoCandidate) {
            PeerConnectionActor.DoCandidate candidate = (PeerConnectionActor.DoCandidate) message;
            sendSignalingMessage(candidate.getUid(), candidate.getDeviceId(),
                    new ApiCandidate(0, candidate.getIndex(), candidate.getId(), candidate.getSdp()));
        } else if (message instanceof DoEndCall) {
            doEndCall();
        } else if (message instanceof PeerConnectionActor.OnStreamAdded) {
            PeerConnectionActor.OnStreamAdded streamAdded = (PeerConnectionActor.OnStreamAdded) message;
            onStreamAdded(streamAdded.getUid(), streamAdded.getDeviceId(), streamAdded.getStream());
        } else if (message instanceof PeerConnectionActor.OnStreamRemoved) {
            PeerConnectionActor.OnStreamRemoved streamRemoved = (PeerConnectionActor.OnStreamRemoved) message;
            onStreamRemoved(streamRemoved.getUid(), streamRemoved.getDeviceId(), streamRemoved.getStream());
        } else if (message instanceof Mute) {
            onMute();
        } else if (message instanceof Unmute) {
            onUnmute();
        } else {
            super.onReceive(message);
        }
    }

    public static class DoEndCall {

    }

    public static class Mute {

    }

    public static class Unmute {

    }
}