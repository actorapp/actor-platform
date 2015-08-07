/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

public interface ModelChangedListener<T> {
    @ObjectiveCName("onChanged:")
    void onChanged(T model);
}
