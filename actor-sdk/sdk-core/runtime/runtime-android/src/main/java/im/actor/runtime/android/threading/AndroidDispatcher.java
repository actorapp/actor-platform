package im.actor.runtime.android.threading;

import android.os.Handler;
import android.os.HandlerThread;

import im.actor.runtime.threading.Dispatcher;

public class AndroidDispatcher implements Dispatcher {

    private HandlerThread handlerThread;
    private Handler handler;

    public AndroidDispatcher() {
        handlerThread = new HandlerThread("dispatcher", Thread.NORM_PRIORITY);
        handlerThread.start();

        // Wait for Looper ready
        while (handlerThread.getLooper() == null) {

        }

        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void dispatch(Runnable message, long delay) {
        handler.postDelayed(message, delay);
    }
}
