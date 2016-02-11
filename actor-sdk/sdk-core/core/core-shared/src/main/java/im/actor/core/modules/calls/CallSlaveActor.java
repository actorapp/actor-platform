package im.actor.core.modules.calls;

import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.Log;

public class CallSlaveActor extends CallActor {

    private static final String TAG = "CallMasterActor";

    public CallSlaveActor(String busId, ModuleContext context) {
        super(busId, context);
    }


    @Override
    public void onBusJoined() {
        Log.d(TAG, "onBusCreated");
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
    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
        Log.d(TAG, "onSignalingMessage");
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
