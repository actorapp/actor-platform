package im.actor.core.modules.calls;

import im.actor.core.api.ApiOffer;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Log;
import im.actor.runtime.WebRTC;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCLocalStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class CallMasterActor extends CallActor {

    private static final String TAG = "CallMasterActor";

    private final Peer peer;

    public CallMasterActor(Peer peer, ModuleContext context) {
        super(context);
        this.peer = peer;
    }

    @Override
    public void onBusCreated() {
        Log.d(TAG, "onBusCreated");
        api(new RequestDoCall(buidOutPeer(peer), getBusId())).then(new Consumer<ResponseDoCall>() {
            @Override
            public void apply(ResponseDoCall responseDoCall) {
                Log.d(TAG, "onBusCreated:result");
                onCallCreated();
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "onBusCreated:error");
                dispose();
            }
        }).done(self());
    }

    public void onCallCreated() {

    }

    @Override
    public void onDeviceConnected(final int uid, final long deviceId) {
        Log.d(TAG, "onDeviceConnected");
        getPeer(uid, deviceId).send(new PeerConnectionActor.OnOfferNeeded());
    }

    @Override
    public void onDeviceDisconnected(int uid, long deviceId) {
        Log.d(TAG, "onDeviceDisconnected");
    }

    @Override
    public void onBusShutdown() {
        Log.d(TAG, "onBusShutdown");
    }

    @Override
    public void onBusDisposed() {
        Log.d(TAG, "onBusDisposed");
    }

    @Override
    public void onBusStopped() {
        Log.d(TAG, "onBusStopped");
    }
}