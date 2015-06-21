/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm.generics;

import im.actor.model.entity.Avatar;
import im.actor.model.mvvm.ValueModel;

public class AvatarValueModel extends ValueModel<Avatar> {

    /**
     * Create ValueModel
     *
     * @param name         name of variable
     * @param defaultValue default value
     */
    public AvatarValueModel(String name, Avatar defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Avatar get() {
        return super.get();
    }
}
