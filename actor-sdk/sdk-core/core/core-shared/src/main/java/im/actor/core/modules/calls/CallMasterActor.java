package im.actor.core.modules.calls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiOnAnswer;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.AbsCallActor;
import im.actor.core.modules.calls.peers.PeerSettings;
import im.actor.core.modules.calls.peers.PeerState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.function.Consumer;

public class CallMasterActor extends AbsCallActor {

    private final Peer peer;
    private long callId;
    private CallVM callVM;
    private ArrayList<PeerStateHolder> peerStates = new ArrayList<>();
    private HashMap<Integer, PeerStateHolder> statesMap = new HashMap<>();
    private CommandCallback<Long> callback;
    private HashSet<Long> readyDevices = new HashSet<>();

    public CallMasterActor(Peer peer, ModuleContext context, CommandCallback<Long> callback) {
        super(context);
        this.peer = peer;
        this.callback = callback;
    }

    @Override
    public void callPreStart() {
        callBus.startMaster();
        peerCall.onOwnStarted();
    }

    @Override
    public void onBusStarted(String busId) {
        api(new RequestDoCall(buidOutPeer(peer), busId)).then(new Consumer<ResponseDoCall>() {
            @Override
            public void apply(ResponseDoCall responseDoCall) {

                //
                // Initialization of Call State
                //
                // TODO: Possible race conditions when members changed during call initiation
                // Need to return explicit callers in response

                if (peer.getPeerType() == PeerType.GROUP) {
                    for (GroupMember gm : getGroup(peer.getPeerId()).getMembers()) {
                        if (gm.getUid() != myUid()) {
                            PeerStateHolder st = new PeerStateHolder(gm.getUid());
                            peerStates.add(st);
                            statesMap.put(st.getUid(), st);
                        }
                    }
                } else if (peer.getPeerType() == PeerType.PRIVATE) {
                    PeerStateHolder st = new PeerStateHolder(peer.getPeerId());
                    peerStates.add(st);
                    statesMap.put(st.getUid(), st);
                } else {
                    throw new RuntimeException("Unsupported Peer Type group");
                }

                //
                // Initialization of CallVM
                //
                callId = responseDoCall.getCallId();
                callVM = callViewModels.spawnNewOutgoingVM(responseDoCall.getCallId(), peer);
                // callVM.getIsMuted().change(isMuted());

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
    public void onAnswered(int uid, long deviceId, PeerSettings settings) {

        // TODO: Handle own calls
        PeerStateHolder peerState = statesMap.get(uid);
        if (peerState == null) {
            return;
        }
        peerState.setWasAnswered(true);

        peerCall.onTheirStarted(deviceId);
        onPeerStarted(uid, deviceId, settings);
        for (long d : readyDevices) {
            if (d != deviceId) {
                callBus.sendSignal(d, new ApiOnAnswer(uid, deviceId));
            }
        }

        broadcastMembers();
    }

    @Override
    public void onAdvertised(int uid, long deviceId, PeerSettings settings) {

        // TODO: Handle own calls
        PeerStateHolder peerState = statesMap.get(uid);
        if (peerState == null) {
            return;
        }
        peerState.setIsConnected(true);

        peerCall.onAdvertised(deviceId, settings);
        if (selfSettings.isPreConnectionEnabled() && settings.isPreConnectionEnabled()) {
            onPeerStarted(uid, deviceId, settings);
        }

        broadcastMembers();
    }

    private void onPeerStarted(int uid, long deviceId, PeerSettings settings) {

        if (readyDevices.contains(deviceId)) {
            return;
        }

        peerCall.onOfferNeeded(deviceId);
        for (long d : readyDevices) {
            callBus.sendSignal(d, new ApiNeedOffer(uid, deviceId, settings.toApi(), false));
        }

        readyDevices.add(deviceId);
    }

    private void broadcastMembers() {

    }

    @Override
    public void onPeerStateChanged(int uid, long deviceId, PeerState state) {
        PeerStateHolder peerState = statesMap.get(uid);
        if (peerState == null) {
            return;
        }

        switch (state) {
            case PENDING:
                // Do Nothing
                break;
            case CONNECTING:
                break;
            case CONNECTED:
                break;
            case ACTIVE:
                // peerState.setIsAnswered(true);
                break;
            case DISPOSED:
//                if (peerState.isAnswered()) {
//                    peerState.setIsRejected(true);
//                }
                readyDevices.remove(deviceId);
                break;
        }

        broadcastMembers();
    }

    @Override
    public void postStop() {
        super.postStop();
        if (callback != null) {
            callback.onError(new RuntimeException());
            callback = null;
        }
    }

    private class PeerStateHolder {

        private int uid;
        private boolean isConnected = false;
        private boolean wasAnswered = false;
        private boolean isRejected = false;

        public PeerStateHolder(int uid) {
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }

        public boolean isConnected() {
            return isConnected;
        }

        public void setIsConnected(boolean isConnected) {
            this.isConnected = isConnected;
        }

        public boolean isWasAnswered() {
            return wasAnswered;
        }

        public void setWasAnswered(boolean wasAnswered) {
            this.wasAnswered = wasAnswered;
        }

        public boolean isRejected() {
            return isRejected;
        }

        public void setIsRejected(boolean isRejected) {
            this.isRejected = isRejected;
        }
    }
}
