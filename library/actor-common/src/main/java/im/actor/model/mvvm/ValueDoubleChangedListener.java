/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

public interface ValueDoubleChangedListener<T, V> {
    void onChanged(T val, ValueModel<T> valueModel, V val2, ValueModel<V> valueModel2);
}
