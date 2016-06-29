package im.actor.sdk.view;

import im.actor.runtime.Runtime;
import im.actor.runtime.actors.Cancellable;

public class ViewAsyncDispatch {

    private ThreadLocal<CancellableRes> currentCancellable = new ThreadLocal<>();

    public Cancellable dispatch(Runnable runnable) {
        CancellableRes res = new CancellableRes();
        Runtime.dispatch(() -> {
            if (res.isCanceled()) {
                return;
            }
            currentCancellable.set(res);
            runnable.run();
            currentCancellable.set(null);
        });
        return res;
    }

    public void complete(Runnable runnable) {
        CancellableRes res = currentCancellable.get();
        Runtime.postToMainThread(() -> {
            if (res.isCanceled()) {
                return;
            }
            runnable.run();
        });
    }

    private static class CancellableRes implements Cancellable {

        private boolean isCanceled = false;

        public boolean isCanceled() {
            return isCanceled;
        }

        @Override
        public void cancel() {
            isCanceled = true;
        }
    }
}
