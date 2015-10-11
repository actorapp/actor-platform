/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel.generics;

import im.actor.runtime.mvvm.ValueModel;

public class BooleanValueModel extends ValueModel<Boolean> {
    /**
     * Create ValueModel
     *
     * @param name         name of variable
     * @param defaultValue default value
     */
    public BooleanValueModel(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Boolean get() {
        return super.get();
    }
}
