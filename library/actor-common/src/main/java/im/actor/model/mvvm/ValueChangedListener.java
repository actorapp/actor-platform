/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

public interface ValueChangedListener<T> {
    void onChanged(T val, ValueModel<T> valueModel);
}