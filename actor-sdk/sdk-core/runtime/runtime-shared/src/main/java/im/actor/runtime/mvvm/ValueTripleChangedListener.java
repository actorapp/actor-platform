/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

public interface ValueTripleChangedListener<T, V, S> {
    @ObjectiveCName("onChanged:withModel:withValue2:withModel2:withValue3:withModel3:")
    void onChanged(T val, Value<T> valueModel, V val2, Value<V> valueModel2,
                   S val3, Value<S> valueModel3);
}
