/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel.generics;

import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

public class ValueModelUserLink extends ValueModel<ArrayListUserLink> {
    /**
     * Create ValueModel
     *
     * @param name         name of variable
     * @param defaultValue default value
     */
    public ValueModelUserLink(String name, ArrayListUserLink defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public ArrayListUserLink get() {
        return super.get();
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListUserLink> listener) {
        super.subscribe(listener);
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListUserLink> listener, boolean notify) {
        super.subscribe(listener, notify);
    }

    @Override
    public void unsubscribe(ValueChangedListener<ArrayListUserLink> listener) {
        super.unsubscribe(listener);
    }
}
