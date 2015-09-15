/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

public interface ValueDoubleChangedListener<T, V> {
    @ObjectiveCName("onChanged:withModel:withValue2:withModel2:")
    void onChanged(T val, Value<T> valueModel, V val2, Value<V> valueModel2);
}
