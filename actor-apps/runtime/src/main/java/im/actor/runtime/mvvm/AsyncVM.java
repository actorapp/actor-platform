/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mvvm;

public abstract class AsyncVM {

    private boolean isDetached;

    protected final void post(final Object obj) {
        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
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