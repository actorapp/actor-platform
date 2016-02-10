package im.actor.core.modules.calls;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.api.rpc.RequestDoCall;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.eventbus.EventBusActor;
import im.actor.runtime.Log;
import im.actor.runtime.function.Consumer;

public class CallMasterActor extends EventBusActor {

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
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                Log.d(TAG, "onBusCreated:error");
                dispose();
            }
        }).done(self());
    }

    @Override
    public void onDeviceConnected(int uid, long deviceId) {
        Log.d(TAG, "onDeviceConnected");
    }

    @Override
    public void onDeviceDisconnected(int uid, long deviceId) {
        Log.d(TAG, "onDeviceDisconnected");
    }

    @Override
    public void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {
        ApiWebRTCSignaling signaling;
        try {
            signaling = ApiWebRTCSignaling.fromBytes(data);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onMessageReceived:ignoring");
            return;
        }

        Log.d(TAG, "onMessageReceived: " + signaling);
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