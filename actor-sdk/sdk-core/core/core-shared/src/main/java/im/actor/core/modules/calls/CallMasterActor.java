package im.actor.core.modules.calls;

import java.util.ArrayList;

import im.actor.core.api.ApiAnswerCall;
import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiSwitchMaster;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.CallState;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.function.Consumer;

public class CallMasterActor extends CallActor {

    private static final String TAG = "CallMasterActor";

    private final Peer peer;
    private CommandCallback<Long> callback;
    private ArrayList<ConnectedHolder> connectedDevices = new ArrayList<>();

    public CallMasterActor(Peer peer, ModuleContext context, CommandCallback<Long> callback) {
        super(context);
        this.callback = callback;
        this.peer = peer;
    }

    @Override
    public void preStart() {
        super.preStart();
        createBus();
    }

    @Override
    public void onBusCreated() {
        api(new RequestDoCall(buidOutPeer(peer), getBusId())).then(new Consumer<ResponseDoCall>() {
            @Override
            public void apply(ResponseDoCall responseDoCall) {
                context().getCallsModule().getCallManager().send(
                        new CallManagerActor.OnCallCreated(responseDoCall.getCallId()), self());
                // TODO: Move to call actor
                context().getCallsModule().spawnNewModel(responseDoCall.getCallId(),
                        peer, new ArrayList<Integer>(), CallState.CALLING_OUTGOING);

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
        sendSignalingMessage(uid, deviceId, new ApiSwitchMaster());
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
        } else {
            super.onSignalingMessage(fromUid, fromDeviceId, signaling);
        }
    }

    @Override
    public void onBusDisposed() {
        super.onBusDisposed();
        if (callback != null) {
            callback.onError(new RuntimeException("Internal Error"));
        }
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
}