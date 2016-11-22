package im.actor.sdk.view;

import android.os.Handler;
import android.os.HandlerThread;

import im.actor.runtime.Runtime;
import im.actor.runtime.function.Cancellable;
import im.actor.runtime.function.CancellableSimple;

public class ViewAsyncDispatch {

    private static final HandlerThread THREAD = new HandlerThread("async_view");
    private static final Handler HANDLER;

    static {
        THREAD.start();
        HANDLER = new Handler(THREAD.getLooper());
    }

    private ThreadLocal<Cancellable> currentCancellable = new ThreadLocal<>();

    public Cancellable dispatch(Runnable runnable) {
        CancellableSimple res = new CancellableSimple();
        dispatch(res, runnable);
        return res;
    }

    public void dispatch(Cancellable cancellable, Runnable runnable) {
        HANDLER.post(() -> {
            if (cancellable.isCancelled()) {
                return;
            }
            currentCancellable.set(cancellable);
            runnable.run();
            currentCancellable.set(null);
        });
    }

    public void complete(Runnable runnable) {
        Cancellable res = currentCancellable.get();
        Runtime.postToMainThread(() -> {
            if (res.isCancelled()) {
                return;
            }
            runnable.run();
        });
    }
}
