package com.droidkit.mvvm.notificators;

import com.droidkit.mvvm.ValueChangeListener;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class ValueUiNotificator<S> extends UiNotificator<ValueChangeListener<S>, S> {

    @Override
    protected void notify(ValueChangeListener<S> subscriber, S value) {
        subscriber.onChanged(value);
    }
}
