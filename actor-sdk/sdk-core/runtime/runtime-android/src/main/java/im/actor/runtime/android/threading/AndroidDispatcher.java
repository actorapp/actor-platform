package im.actor.runtime.android.threading;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import im.actor.runtime.threading.DispatchCancel;
import im.actor.runtime.threading.Dispatcher;

public class AndroidDispatcher implements Dispatcher {

    private HandlerThread handlerThread;
    private Handler handler;

    public AndroidDispatcher(String name) {
        handlerThread = new HandlerThread(name, Thread.NORM_PRIORITY);
        handlerThread.start();

        // Wait for Looper ready
        while (handlerThread.getLooper() == null) {

        }

        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public DispatchCancel dispatch(final Runnable message, long delay) {
        final Object o = new Object();
        handler.postAtTime(message, o, SystemClock.uptimeMillis() + delay);
        return () -> handler.removeCallbacks(message, o);
    }
}
