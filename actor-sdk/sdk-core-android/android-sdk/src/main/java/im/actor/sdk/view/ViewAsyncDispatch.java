package im.actor.sdk.view;

import im.actor.runtime.Runtime;
import im.actor.runtime.function.Cancellable;
import im.actor.runtime.function.CancellableSimple;

public class ViewAsyncDispatch {

    private ThreadLocal<Cancellable> currentCancellable = new ThreadLocal<>();

    public Cancellable dispatch(Runnable runnable) {
        CancellableSimple res = new CancellableSimple();
        dispatch(res, runnable);
        return res;
    }

    public void dispatch(Cancellable cancellable, Runnable runnable) {
        Runtime.dispatch(() -> {
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
