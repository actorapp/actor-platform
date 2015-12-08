/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel.generics;

import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

public class ValueModelUserEmail extends ValueModel<ArrayListUserEmail> {
    /**
     * Create ValueModel
     *
     * @param name         name of variable
     * @param defaultValue default value
     */
    public ValueModelUserEmail(String name, ArrayListUserEmail defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public ArrayListUserEmail get() {
        return super.get();
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListUserEmail> listener) {
        super.subscribe(listener);
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListUserEmail> listener, boolean notify) {
        super.subscribe(listener, notify);
    }

    @Override
    public void unsubscribe(ValueChangedListener<ArrayListUserEmail> listener) {
        super.unsubscribe(listener);
    }
}
