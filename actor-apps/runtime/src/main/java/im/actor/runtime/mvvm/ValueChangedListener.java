/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

public interface ValueChangedListener<T> {
    @ObjectiveCName("onChanged:withModel:")
    void onChanged(T val, Value<T> valueModel);
}