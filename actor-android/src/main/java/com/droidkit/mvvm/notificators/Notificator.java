package com.droidkit.mvvm.notificators;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 17.09.14.
 */
public abstract class Notificator<S, V> {
    protected final ArrayList<S> subscribers = new ArrayList<S>();

    public void subscribe(S subs) {
        synchronized (subscribers) {
            if (!subscribers.contains(subs)) {
                subscribers.add(subs);
            }
        }
    }

    public void unsubscribe(S subs) {
        synchronized (subscribers) {
            subscribers.remove(subs);
        }
    }

    public abstract void notify(final V value);
}
