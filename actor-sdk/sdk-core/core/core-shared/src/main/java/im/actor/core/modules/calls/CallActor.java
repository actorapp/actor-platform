package im.actor.core.modules.calls;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;

import im.actor.core.api.ApiAnswer;
import im.actor.core.api.ApiCandidate;
import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;

public class CallActor extends EventBusActor {

    private static final String TAG = "CallActor";

    private HashMap<Integer, HashMap<Long, ActorRef>> peerConnections = new HashMap<>();

    public CallActor(ModuleContext context) {
        super(context);
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
        // Log.d(TAG, "sendSignaling");
        try {
            sendMessage(uid, deviceId, signaling.buildContainer());
        } catch (IOException e) {
            e.printStackTrace();
            // Ignore
        }
    }

    @Override
    public final void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
        // Log.d(TAG, "onMessageReceived:start");

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
        // Log.d(TAG, "onMessageReceived: " + signaling + ": end");
    }

    @Override
    public void onReceive(Object message) {
        // Log.d(TAG, "onReceive");
        if (message instanceof PeerConnectionActor.DoAnswer) {
            //Log.d(TAG, "onReceive:doAnswer");
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
        } else {
            super.onReceive(message);
        }
        //Log.d(TAG, "onReceive:End");
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