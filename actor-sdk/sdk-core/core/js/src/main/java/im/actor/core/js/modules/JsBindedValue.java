/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.modules;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class JsBindedValue<T> {

    private T value;
    private ArrayList<JsBindedValueCallback> callbacks = new ArrayList<JsBindedValueCallback>();

    public JsBindedValue(T value) {
        this.value = value;
    }

    public JsBindedValue() {

    }

    public T get() {
        return value;
    }

    public void subscribe(JsBindedValueCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
            callback.onChanged(value);
        }
    }

    public void unsubscribe(JsBindedValueCallback callback) {
        callbacks.remove(callback);
    }

    public void changeValue(T value) {
        this.value = value;
        for (JsBindedValueCallback callback : callbacks) {
            callback.onChanged(value);
        }
    }
}
