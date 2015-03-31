package im.actor.android;

import android.os.Handler;
import android.os.HandlerThread;

import im.actor.model.DispatcherProvider;

/**
 * Created by ex3ndr on 24.03.15.
 */
public class AndroidCallbackDispatcher implements DispatcherProvider {

    private Handler handler;
    private final HandlerThread handlerThread;

    public AndroidCallbackDispatcher() {
        handlerThread = new HandlerThread("CallbacksThread", Thread.MIN_PRIORITY) {
            @Override
            protected void onLooperPrepared() {
                handler = new Handler(getLooper());
            }
        };
        handlerThread.start();
    }

    @Override
    public void dispatch(Runnable runnable) {
        while (handler == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        handler.post(runnable);
    }
}
