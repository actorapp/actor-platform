package im.actor.model.android;

import android.os.Handler;
import android.os.Looper;

import im.actor.model.MainThread;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class AndroidMainThread implements MainThread {
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }
}
