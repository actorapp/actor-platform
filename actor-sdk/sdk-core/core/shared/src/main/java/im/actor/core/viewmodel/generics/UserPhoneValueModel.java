/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel.generics;

import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

public class UserPhoneValueModel extends ValueModel<ArrayListUserPhone> {
    /**
     * Create ValueModel
     *
     * @param name         name of variable
     * @param defaultValue default value
     */
    public UserPhoneValueModel(String name, ArrayListUserPhone defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public ArrayListUserPhone get() {
        return super.get();
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListUserPhone> listener) {
        super.subscribe(listener);
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListUserPhone> listener, boolean notify) {
        super.subscribe(listener, notify);
    }

    @Override
    public void unsubscribe(ValueChangedListener<ArrayListUserPhone> listener) {
        super.unsubscribe(listener);
    }
}
