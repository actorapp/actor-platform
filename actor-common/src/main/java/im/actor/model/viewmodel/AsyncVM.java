package im.actor.model.viewmodel;

import im.actor.model.mvvm.MVVMEngine;

/**
 * Created by ex3ndr on 26.02.15.
 */
public abstract class AsyncVM {
    private boolean isDetached;

    protected final void post(final Object obj) {
        MVVMEngine.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isDetached) {
                    onObjectReceived(obj);
                }
            }
        });
    }

    protected abstract void onObjectReceived(Object obj);

    public void detach() {
        isDetached = true;
    }
}