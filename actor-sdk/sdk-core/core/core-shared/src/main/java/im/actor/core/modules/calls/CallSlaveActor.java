package im.actor.core.modules.calls;

import im.actor.core.modules.ModuleContext;
import im.actor.runtime.Log;

public class CallSlaveActor extends CallActor {

    private static final String TAG = "CallSlaveActor";

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

    @Override
    public void onReceive(Object message) {
        Log.d(TAG,"onReceive");
        super.onReceive(message);
        Log.d(TAG, "onReceive:end");
    }
}
