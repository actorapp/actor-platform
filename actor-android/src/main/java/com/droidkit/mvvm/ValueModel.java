package com.droidkit.mvvm;

import com.droidkit.mvvm.notificators.ValueBackgroundNotificator;
import com.droidkit.mvvm.notificators.ValueUiNotificator;

/**
 * Created by ex3ndr on 15.09.14.
 */
public class ValueModel<T> extends BaseModel {

    private ValueUiNotificator<T> uiNotificator = new ValueUiNotificator<T>();
    private ValueBackgroundNotificator<T> bgNotificator = new ValueBackgroundNotificator<T>();

    private T value;

    public ValueModel(String name, T initialValue) {
        super(name);
        change(initialValue);
    }

    public void addUiSubscriber(ValueChangeListener<T> sub) {
        uiNotificator.subscribe(sub);
        sub.onChanged(value);
    }

    public void removeUiSubscriber(ValueChangeListener<T> sub) {
        uiNotificator.unsubscribe(sub);
    }

    public void addSubscriber(ValueChangeListener<T> sub) {
        bgNotificator.subscribe(sub);
        sub.onChanged(value);
    }

    public void removeSubscriber(ValueChangeListener<T> sub) {
        bgNotificator.unsubscribe(sub);
    }

    public T getValue() {
        return value;
    }

    public void change(T value) {
        if (this.value != null && value != null && value.equals(this.value)) {
            return;
        }

        this.value = value;
        uiNotificator.notify(value);
        bgNotificator.notify(value);
    }
}
