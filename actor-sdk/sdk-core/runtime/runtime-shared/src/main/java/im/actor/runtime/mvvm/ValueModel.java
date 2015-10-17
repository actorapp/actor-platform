package im.actor.runtime.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

public class ValueModel<T> extends Value<T> {

    @Property("nonatomic, readonly")
    private T value;

    public ValueModel(String name, T defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    @Override
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

    @ObjectiveCName("changeNoNotificationWithValue:")
    public boolean changeNoNotification(T value) {
        if (this.value != null && value != null && value.equals(this.value)) {
            return false;
        }

        // No need in sync. We are not expected complex sync of value models
        this.value = value;

        return true;
    }

    @ObjectiveCName("changeInUIThreadWithValue:")
    protected boolean changeInUIThread(T value) {
        if (this.value != null && value != null && value.equals(this.value)) {
            return false;
        }

        this.value = value;

        notifyInMainThread(value);

        return true;
    }

    /**
     * Forcing notify about value change
     */
    @ObjectiveCName("forceNotify")
    public void forceNotify() {
        notify(value);
    }
}
