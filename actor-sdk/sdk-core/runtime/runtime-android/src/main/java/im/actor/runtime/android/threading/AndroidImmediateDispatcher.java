package im.actor.runtime.android.threading;

import android.os.Handler;
import android.os.HandlerThread;

import im.actor.runtime.actors.ThreadPriority;
import im.actor.runtime.threading.ImmediateDispatcher;

public class AndroidImmediateDispatcher implements ImmediateDispatcher {

    private HandlerThread handlerThread;
    private Handler handler;

    public AndroidImmediateDispatcher(String name, ThreadPriority priority) {

        handlerThread = new HandlerThread(name);
        switch (priority) {
            case HIGH:
                handlerThread.setPriority(Thread.MAX_PRIORITY);
            case LOW:
                handlerThread.setPriority(Thread.MIN_PRIORITY);
            default:
            case NORMAL:
                handlerThread.setPriority(Thread.NORM_PRIORITY);
        }
        handlerThread.start();

        // Wait for Looper ready
        while (handlerThread.getLooper() == null) {

        }

        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void dispatchNow(Runnable runnable) {
        handler.post(runnable);
    }
}
