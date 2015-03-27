package im.actor.model.mvvm;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class ValueModel<T> {

    private ArrayList<ValueChangedListener<T>> listeners = new ArrayList<ValueChangedListener<T>>();
    private String name;
    private volatile T value;

    public ValueModel(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public T get() {
        return value;
    }

    public boolean change(T value) {
        if (this.value != null && value != null && value.equals(this.value)) {
            return false;
        }

        // No need in sync. We are not expected complex sync of value models
        this.value = value;

        notify(value);

        return true;
    }

    // We expect that subscribe will be called only on UI Thread
    public void subscribe(ValueChangedListener<T> listener) {
        MVVMEngine.checkMainThread();
        subscribe(listener, true);
    }

    // We expect that subscribe will be called only on UI Thread
    public void subscribe(ValueChangedListener<T> listener, boolean notify) {
        MVVMEngine.checkMainThread();
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        if (notify) {
            listener.onChanged(value, this);
        }
    }

    // We expect that subscribe will be called only on UI Thread
    public void unsubscribe(ValueChangedListener<T> listener) {
        MVVMEngine.checkMainThread();
        listeners.remove(listener);
    }

    private void notify(final T value) {
        MVVMEngine.getMainThreadProvider().runOnUiThread(new Runnable() {
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