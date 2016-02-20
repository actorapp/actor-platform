package im.actor.core.modules.calls;

import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.api.ApiPeerSettings;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.peers.PeerNodeInt;
import im.actor.core.modules.calls.peers.PeerNodeSettings;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;

public class PeerCallMasterActor extends AbsCallActor {

    private static final String TAG = "CallMasterActor";
    private static final long MASTER_CALL_TIMEOUT = 8000;

    private final Peer peer;
    private ActorRef callManager;
    private CommandCallback<Long> callback;
    private HashSet<Integer> members;
    private HashMap<Long, Node> nodes;
    private long callId;
    private CallVM callVM;

    public PeerCallMasterActor(Peer peer, CommandCallback<Long> callback, ModuleContext context) {
        super(false, context);
        this.peer = peer;
        this.callback = callback;
    }

    @Override
    public void preStart() {
        super.preStart();
        getSelfSettings().setIsPreConnectionEnabled(true);
        callManager = context().getCallsModule().getCallManager();
        createBus(MASTER_CALL_TIMEOUT);
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

                members = new HashSet<>();
                if (peer.getPeerType() == PeerType.GROUP) {
                    for (GroupMember gm : getGroup(peer.getPeerId()).getMembers()) {
                        members.add(gm.getUid());
                    }
                } else if (peer.getPeerType() == PeerType.PRIVATE) {
                    members.add(peer.getPeerId());
                    members.add(myUid());
                } else {
                    throw new RuntimeException("Unsupported Peer Type group");
                }

                //
                // Initialization of CallVM
                //
                callId = responseDoCall.getCallId();
                callVM = spanNewOutgoingVM(responseDoCall.getCallId(), peer);

                //
                // Notifying about successful call creation
                //
                callManager.send(new CallManagerActor.DoCallComplete(responseDoCall.getCallId()), self());
                callback.onResult(responseDoCall.getCallId());
                callback = null;

                //
                // Create New Node Collection
                //
                nodes = new HashMap<>();
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
        if (!members.contains(uid) || uid == 0) {
            return;
        }

        Log.d(TAG, "onDeviceConnected:" + deviceId);

        sendSwitchMaster(uid, deviceId);
    }

    @Override
    public void onAdvertised(int uid, long deviceId, ApiPeerSettings settings) {
        if (nodes.containsKey(deviceId)) {
            return;
        }

        Log.d(TAG, "onAdvertised:" + deviceId + ", " + settings);

        PeerNodeSettings nodeSettings = new PeerNodeSettings(settings);
        Node node = new Node(uid, deviceId, getPeer(uid, deviceId), nodeSettings, settings);
        nodes.put(deviceId, node);

        //
        // Starting Pre Connections
        //
        if (nodeSettings.isPreConnectionEnabled()) {

            for (Node n : nodes.values()) {
                if (n.getDeviceId() == deviceId) {
                    continue;
                }
                if (n.isAnswered() || n.getPeerSettings().isPreConnectionEnabled()) {
                    sendNeedOffer(n.getUid(), n.getDeviceId(), uid, deviceId, settings, true);
                    node.getConnectedDevices().put(n.getDeviceId(), ConnectionState.SILENT);
                    n.getConnectedDevices().put(node.getDeviceId(), ConnectionState.SILENT);
                }
            }

            if (getSelfSettings().isPreConnectionEnabled()) {
                getPeer(uid, deviceId).onAdvertised(nodeSettings);
                getPeer(uid, deviceId).onOfferNeeded();
            }
        }
    }

    @Override
    public void onAnswered(int uid, long deviceId) {
        super.onAnswered(uid, deviceId);

        Log.d(TAG, "onAnswered:" + deviceId);

        if (!nodes.containsKey(deviceId)) {
            return;
        }

        Node node = nodes.get(deviceId);
        if (node.getPeerSettings().isPreConnectionEnabled() && getSelfSettings().isPreConnectionEnabled()) {
            getPeer(uid, deviceId).onAnswered();
        } else {
            getPeer(uid, deviceId).onAdvertised(node.getPeerSettings());
            getPeer(uid, deviceId).onOfferNeeded();
            getPeer(uid, deviceId).onAnswered();
        }

        //
        // Starting Pre Connections
        //
        for (Node n : nodes.values()) {
            if (n.getDeviceId() == deviceId) {
                continue;
            }
            if (n.isAnswered() || n.getPeerSettings().isPreConnectionEnabled()) {
                ConnectionState connectionState = n.getConnectedDevices().get(deviceId);
                if (connectionState == null) {
                    sendNeedOffer(n.getUid(), n.getDeviceId(), uid, deviceId, n.getApiPeerSettings(), false);
                } else if (connectionState == ConnectionState.SILENT) {
                    sendOnAnswered(n.getUid(), n.getDeviceId(), uid, deviceId);
                    n.getConnectedDevices().put(deviceId, ConnectionState.CONNECTED);
                } else {
                    // Already connected
                }
            }
        }
    }

    @Override
    public void onPeerConnected(int uid, long deviceId) {
        super.onPeerConnected(uid, deviceId);
        Log.d(TAG, "onPeerConnected:" + deviceId);
    }

    @Override
    public void onPeerStarted(int uid, long deviceId) {
        super.onPeerStarted(uid, deviceId);
        Log.d(TAG, "onPeerStarted:" + deviceId);
    }

    @Override
    public void onDeviceDisconnected(int uid, long deviceId) {

    }

    @Override
    public void postStop() {
        super.postStop();

        if (callback != null) {
            callback.onError(new RuntimeException("Internal Error"));
        }

        if (callVM != null) {
            callVM.getState().change(CallState.ENDED);
            callManager.send(new CallManagerActor.OnCallEnded(callId));
        }
    }

    private enum ConnectionState {
        SILENT, CONNECTED
    }

    private class Node {

        private int uid;
        private long deviceId;
        private PeerNodeInt nodeInt;
        private PeerNodeSettings peerSettings;
        private ApiPeerSettings apiPeerSettings;
        private boolean isAnswered;
        private HashMap<Long, ConnectionState> connectedDevices = new HashMap<>();

        public Node(int uid, long deviceId, PeerNodeInt nodeInt, PeerNodeSettings peerSettings,
                    ApiPeerSettings apiPeerSettings) {
            this.uid = uid;
            this.deviceId = deviceId;
            this.nodeInt = nodeInt;
            this.peerSettings = peerSettings;
            this.isAnswered = false;
            this.apiPeerSettings = apiPeerSettings;
        }

        public HashMap<Long, ConnectionState> getConnectedDevices() {
            return connectedDevices;
        }

        public ApiPeerSettings getApiPeerSettings() {
            return apiPeerSettings;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }

        public boolean isAnswered() {
            return isAnswered;
        }

        public void setIsAnswered(boolean isAnswered) {
            this.isAnswered = isAnswered;
        }

        public PeerNodeInt getNodeInt() {
            return nodeInt;
        }

        public PeerNodeSettings getPeerSettings() {
            return peerSettings;
        }
    }
}
