/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm.generics;

import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 22.05.15.
 */
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
