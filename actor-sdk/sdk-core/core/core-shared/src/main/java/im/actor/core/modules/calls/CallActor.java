package im.actor.core.modules.calls;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiAnswer;
import im.actor.core.api.ApiCandidate;
import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.webrtc.WebRTCLocalStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class CallActor extends EventBusActor {

    private static final String TAG = "CallActor";

    private HashMap<Integer, HashMap<Long, ActorRef>> peerConnections = new HashMap<>();

    public CallActor(ModuleContext context) {
        super(context);
    }

    public CallActor(String busId, ModuleContext context) {
        super(busId, context);
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

    public final void sendSignalingMessage(int uid, long deviceId, ApiWebRTCSignaling signaling) {
        try {
            sendMessage(uid, deviceId, signaling.buildContainer());
        } catch (IOException e) {
            e.printStackTrace();
            // Ignore
        }
    }

    @Override
    public final void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
        // Ignoring messages without sender
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
    public void onReceive(Object message) {
        if (message instanceof PeerConnectionActor.DoAnswer) {
            PeerConnectionActor.DoAnswer answer = (PeerConnectionActor.DoAnswer) message;
            sendSignalingMessage(answer.getUid(), answer.getDeviceId(),
                    new ApiAnswer(0, answer.getSdp()));
        } else if (message instanceof PeerConnectionActor.DoOffer) {
            PeerConnectionActor.DoOffer offer = (PeerConnectionActor.DoOffer) message;
            sendSignalingMessage(offer.getUid(), offer.getDeviceId(),
                    new ApiOffer(0, offer.getSdp()));
        } else {
            super.onReceive(message);
        }
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
                PeerConnectionActor.CONSTRUCTOR(self(), uid, deviceId, context()));
        refs.put(deviceId, ref);
        return ref;
    }
}