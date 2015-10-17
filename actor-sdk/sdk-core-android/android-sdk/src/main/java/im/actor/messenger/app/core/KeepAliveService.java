package im.actor.messenger.app.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import im.actor.runtime.Log;

public class KeepAliveService extends Service {
    public KeepAliveService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("KL", "KEEP ALIVE SERVICE STARTED");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
