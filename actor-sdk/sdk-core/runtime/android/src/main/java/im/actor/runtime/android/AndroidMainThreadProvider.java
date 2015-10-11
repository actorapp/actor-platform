/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android;

import android.os.Handler;
import android.os.Looper;

import im.actor.runtime.MainThreadRuntime;

public class AndroidMainThreadProvider implements MainThreadRuntime {

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
