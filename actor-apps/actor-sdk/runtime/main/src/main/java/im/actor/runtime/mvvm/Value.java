package im.actor.runtime.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;

/**
 * Bindable MVVM Value. Used in UI data binding
 *
 * @param <T> type of value
 */
public abstract class Value<T> {

    private ArrayList<ValueChangedListener<T>> listeners = new ArrayList<ValueChangedListener<T>>();

    private String name;

    /**
     * Default constructor of value
     *
     * @param name name of Value
     */
    public Value(String name) {
        this.name = name;
    }

    /**
     * Get current value
     *
     * @return the value
     */
    @ObjectiveCName("get")
    public abstract T get();

    /**
     * Getting Name of Value
     * Useful for debugging current bindings and notifications
     *
     * @return name of value
     */
    public String getName() {
        return name;
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
            listener.onChanged(get(), this);
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

    /**
     * Performing notification to subscribers
     *
     * @param value new value
     */
    protected void notify(final T value) {
        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
            @Override
            public void run() {
                notifyInMainThread(value);
            }
        });
    }

    /**
     * Performing notification to subscribers if we know that we are on mainthread
     * Useful for chainging updates from chain of values
     *
     * @param value new value
     */
    protected void notifyInMainThread(final T value) {
        for (ValueChangedListener<T> listener :
                listeners.toArray(new ValueChangedListener[listeners.size()])) {
            listener.onChanged(value, Value.this);
        }
    }
}
