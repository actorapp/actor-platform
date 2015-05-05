/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

public interface ValueTripleChangedListener<T, V, S> {
    void onChanged(T val, ValueModel<T> valueModel, V val2, ValueModel<V> valueModel2,
                          S val3, ValueModel<S> valueModel3);
}
