/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

public interface ValueChangedListener<T> {
    @ObjectiveCName("onChanged:withModel:")
    void onChanged(T val, ValueModel<T> valueModel);
}