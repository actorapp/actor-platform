package im.actor.android;

import android.os.Handler;
import android.os.Looper;

import im.actor.model.MainThreadProvider;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class AndroidMainThreadProvider implements MainThreadProvider {

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void postToMainThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    @Override
    public boolean isSingleThread() {
        return false;
    }
}
