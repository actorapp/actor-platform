/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm.generics;

import im.actor.model.mvvm.ValueModel;

public class StringValueModel extends ValueModel<String> {
    /**
     * Create ValueModel
     *
     * @param name         name of variable
     * @param defaultValue default value
     */
    public StringValueModel(String name, String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public String get() {
        return super.get();
    }
}
