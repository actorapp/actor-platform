package com.droidkit.mvvm.notificators;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by ex3ndr on 17.09.14.
 */
public abstract class UiNotificator<S, V> extends Notificator<S, V> {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public void notify(final V value) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                synchronized (subscribers) {
                    for (S sub : subscribers) {
                        UiNotificator.this.notify(sub, value);
                    }
                }
            }
        });
    }

    protected abstract void notify(S subscriber, V value);
}
