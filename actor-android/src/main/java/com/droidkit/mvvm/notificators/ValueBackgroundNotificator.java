package com.droidkit.mvvm.notificators;

import com.droidkit.mvvm.ValueChangeListener;

/**
 * Created by ex3ndr on 19.09.14.
 */
public class ValueBackgroundNotificator<T> extends BackgroundNotificator<ValueChangeListener, T> {
    @Override
    protected void notify(ValueChangeListener subscriber, T value) {
        subscriber.onChanged(value);
    }
}
