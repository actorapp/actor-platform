/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.angular;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class AngularValue<T> {

    private T value;
    private ArrayList<AngularValueCallback> callbacks = new ArrayList<AngularValueCallback>();

    public AngularValue(T value) {
        this.value = value;
    }

    public AngularValue() {

    }

    public T get() {
        return value;
    }

    public void subscribe(AngularValueCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
            callback.onChanged(value);
        }
    }

    public void unsubscribe(AngularValueCallback callback) {
        callbacks.remove(callback);
    }

    public void changeValue(T value) {
        this.value = value;
        for (AngularValueCallback callback : callbacks) {
            callback.onChanged(value);
        }
    }
}
