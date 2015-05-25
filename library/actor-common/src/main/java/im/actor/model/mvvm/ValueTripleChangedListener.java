/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

public interface ValueTripleChangedListener<T, V, S> {
    @ObjectiveCName("onChanged:withModel:withValue2:withModel2:withValue3:withModel3:")
    void onChanged(T val, ValueModel<T> valueModel, V val2, ValueModel<V> valueModel2,
                          S val3, ValueModel<S> valueModel3);
}
