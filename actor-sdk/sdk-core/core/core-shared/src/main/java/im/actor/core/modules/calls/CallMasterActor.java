package im.actor.core.modules.calls;

import java.util.ArrayList;

import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.function.Consumer;

public class CallMasterActor extends CallActor {

    private static final String TAG = "CallMasterActor";

    private final Peer peer;
    private ArrayList<ConnectedHolder> connectedDevices = new ArrayList<>();

    public CallMasterActor(Peer peer, ModuleContext context) {
        super(context);
        this.peer = peer;
    }

    @Override
    public void onBusCreated() {
        api(new RequestDoCall(buidOutPeer(peer), getBusId())).then(new Consumer<ResponseDoCall>() {
            @Override
            public void apply(ResponseDoCall responseDoCall) {
                onCallCreated();
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                dispose();
            }
        }).done(self());
    }

    public void onCallCreated() {

    }

    @Override
    public void onDeviceConnected(final int uid, final long deviceId) {
        ConnectedHolder connectedHolder = new ConnectedHolder(uid, deviceId);
        if (connectedDevices.contains(connectedHolder)) {
            return;
        }
        getPeer(uid, deviceId).send(new PeerConnectionActor.OnOfferNeeded());
        for (ConnectedHolder c : connectedDevices) {
            // sendSignalingMessage(c.uid, c.deviceId, new ApiNeedOffer(uid, deviceId));
            sendSignalingMessage(uid, deviceId, new ApiNeedOffer(c.uid, c.deviceId));
        }
        connectedDevices.add(connectedHolder);
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