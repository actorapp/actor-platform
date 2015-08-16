/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android;

import android.os.Handler;
import android.os.HandlerThread;

import im.actor.runtime.DispatcherRuntime;

public class AndroidDispatcherProvider implements DispatcherRuntime {

    private Handler handler;
    private final HandlerThread handlerThread;

    public AndroidDispatcherProvider() {
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
