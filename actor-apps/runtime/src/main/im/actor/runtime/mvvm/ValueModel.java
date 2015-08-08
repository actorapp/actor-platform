/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;

public class ValueModel<T> {

    private ArrayList<ValueChangedListener<T>> listeners = new ArrayList<ValueChangedListener<T>>();
    private String name;
    private volatile T value;

    /**
     * Create ValueModel
     *
     * @param name         name of variable
     * @param defaultValue default value
     */
    @ObjectiveCName("initWithName:withValue:")
    public ValueModel(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    /**
     * Get current value
     *
     * @return the value
     */
    @ObjectiveCName("get")
    public T get() {
        return value;
    }

    /**
     * Changing value from any thread. We are not expect simulatenous updates from different threads,
     * just only one thread
     *
     * @param value
     * @return is value changed
     */
    @ObjectiveCName("changeWithValue:")
    public boolean change(T value) {
        if (this.value != null && value != null && value.equals(this.value)) {
            return false;
        }

        // No need in sync. We are not expected complex sync of value models
        this.value = value;

        notify(value);

        return true;
    }

    /**
     * Forcing notify about value change
     */
    @ObjectiveCName("forceNotify")
    public void forceNotify() {
        notify(value);
    }

    /**
     * Subscribe to value updates
     *
     * @param listener update listener
     */
    @ObjectiveCName("subscribeWithListener:")
    public void subscribe(ValueChangedListener<T> listener) {
        subscribe(listener, true);
    }

    /**
     * Subscribe to value updates
     *
     * @param listener update listener
     * @param notify   perform notify about current value
     */
    @ObjectiveCName("subscribeWithListener:notify:")
    public void subscribe(ValueChangedListener<T> listener, boolean notify) {
        im.actor.runtime.Runtime.checkMainThread();

        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        if (notify) {
            listener.onChanged(value, this);
        }
    }

    /**
     * Remove subscription for updates
     *
     * @param listener update listener
     */
    @ObjectiveCName("unsubscribeWithListener:")
    public void unsubscribe(ValueChangedListener<T> listener) {
        im.actor.runtime.Runtime.checkMainThread();

        listeners.remove(listener);
    }

    private void notify(final T value) {
        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
            @Override
            public void run() {
                for (ValueChangedListener<T> listener :
                        listeners.toArray(new ValueChangedListener[listeners.size()])) {
                    listener.onChanged(value, ValueModel.this);
                }
            }
        });
    }

    @Override
    public String toString() {
        return value + "";
    }
}